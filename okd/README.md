# DEI Deployment on OKD / OpenShift

This directory contains manifests and OpenShift Templates for deploying the **DEI** (Data Exchange Infrastructure) application on OKD / OpenShift.

---

## Files Overview

- `template.yaml` – **Recommended:** OpenShift Template (`kind: Template`) combining deployment resources (PVC, ConfigMap, Deployment, Service, Route) with parameter support for multiple environments (`test`, `prod`). Sensitive secrets are referenced via `${APP_NAME}-secret` and managed separately directly in OKD.
- `env-test.params` – Non-sensitive parameter values for the test / staging environment (`dei-staging`).
- `env-prod.params` – Non-sensitive parameter values for the production environment (`dei-production`).
- `01-pvc.yaml` – `PersistentVolumeClaim` (`dei-conversion-pvc`) providing persistent storage for temporary conversion files (mounted at `/data/conversion`).
- `02-configmap.yaml` – `ConfigMap` (`dei-config`) containing test environment configurations and external service endpoints.
- `03-secret.yaml.example` – Example `Secret` definition for sensitive credentials (database credentials and API tokens). To be applied manually or created in OKD Console.
- `04-deployment.yaml` – Application `Deployment` configured with `Recreate` strategy and health probes.
- `05-service.yaml` – Internal cluster `Service` (`ClusterIP`).
- `06-route.yaml` – OpenShift `Route` with SSL termination (`edge`).
- `07-cronjob.yaml` – `CronJob` (`dei-rsync-cronjob`) executing periodic synchronization (every 15 minutes) of conversion output files to the remote IIIF server via `rsync` over SSH (executes `/scripts/sync.sh` mounted from ConfigMap).
- `dei-all.yaml` – Consolidated static manifest containing individual non-sensitive objects (including the rsync CronJob).

---

## Deployment Strategy & Principles

1. **Environment-Agnostic Image**: The container image built by Docker contains pre-compiled Vaadin assets and application code. It is independent of whether it is deployed to test or production. The runtime behavior and configuration are determined via `SPRING_PROFILES_ACTIVE` along with environment variables injected via OKD ConfigMaps and Secrets.
2. **Neutral Image Tagging**: Images pushed to the container registry use versioned or build-based tags (e.g. `build-42` or `1.0.0`) without embedding environment names (such as `test` or `prod`). This ensures that the exact same tested image artifact can be promoted and deployed to production.
3. **Environment Separation**:
   - **Test / Staging**: Target namespace (e.g. `dei-staging`).
   - **Production**: Target namespace (e.g. `dei-production`).
4. **Secret & Credential Management**:
   - Application secrets (`${APP_NAME}-secret`) are maintained directly in OKD (via OKD Web Console or `oc create secret generic`). They are **not** committed to Git and will not be overwritten by CI/CD pipeline deploys.
   - Example command to create application secret:
     ```bash
     # Staging environment
     oc create secret generic dei-staging-secret \
       --from-literal=SPRING_DATASOURCE_URL="jdbc:mysql://db.example.com:3306/dei_staging?useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false" \
       --from-literal=SPRING_DATASOURCE_USERNAME="dei_user" \
       --from-literal=SPRING_DATASOURCE_PASSWORD="staging-db-password" \
       --from-literal=EUROPEANA_NEW_API_TP_AUTHORIZATION_TOKEN="token" \
       --from-literal=API_CLIENT_ID="transcribathon-dei" \
       --from-literal=API_CLIENT_SECRET="client-secret" \
       --from-literal=API_USERNAME="api-username" \
       --from-literal=API_PASSWORD="api-password" \
       --from-literal=DRI_API_TOKEN="dri-token" \
       -n dei-staging

     # Production environment
     oc create secret generic dei-production-secret \
       --from-literal=SPRING_DATASOURCE_URL="jdbc:mysql://db.example.com:3306/dei_prod?useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false" \
       --from-literal=SPRING_DATASOURCE_USERNAME="dei_user" \
       --from-literal=SPRING_DATASOURCE_PASSWORD="production-db-password" \
       --from-literal=EUROPEANA_NEW_API_TP_AUTHORIZATION_TOKEN="production-token" \
       --from-literal=API_CLIENT_ID="production-client-id" \
       --from-literal=API_CLIENT_SECRET="production-client-secret" \
       --from-literal=API_USERNAME="production-username" \
       --from-literal=API_PASSWORD="production-password" \
       --from-literal=DRI_API_TOKEN="production-dri-token" \
       -n dei-production

     # SSH Secrets for Rsync CronJob:
     # 1. Generate ed25519 SSH key pair:
     # ssh-keygen -t ed25519 -C "dei-rsync-key" -f ~/.ssh/id_ed25519
     #
     # 2. Staging SSH key secret:
     oc create secret generic dei-staging-ssh-secret \
       --from-file=id_ed25519=~/.ssh/id_ed25519 \
       -n dei-staging

     # 3. Production SSH key secret:
     oc create secret generic dei-production-ssh-secret \
       --from-file=id_ed25519=~/.ssh/id_ed25519 \
       -n dei-production
     ```
   - OKD tokens are stored in Jenkins Credentials as Secret Text (`okd-token` for test / staging and `okd-token-production` for production).
   - OKD API Server URL and Namespace are defined in the private parameter files (`OKD_SERVER=...` and `OKD_NAMESPACE=...` in `env-test-private.params` / `env-prod-private.params` in GitLab).
   - GitLab Deploy Token for HTTPS access is stored in Jenkins Credentials as Username/Password (`gitlab-config-deploy-token`).
   - GitLab repository HTTPS URL is stored in Jenkins Credentials as Secret Text (`gitlab-config-repo-https-url`).
   - GitLab Deploy Key for SSH access is stored in Jenkins Credentials as SSH Username with private key (`gitlab-config-repo-ssh-key`).
   - GitLab repository SSH URL is stored in Jenkins Credentials as Secret Text (`gitlab-config-repo-url`).
   - Container registry credentials are stored in Jenkins Credentials as Username/Password (`harbor-creds` or `container-registry-creds`).

---

## Deployment via OpenShift Template (`template.yaml`)

Using `template.yaml` allows parameterizing all variables and deploying with a single command.

### 1. Direct CLI Deployment (Test / Staging)

```bash
oc process -f okd/template.yaml --param-file=okd/env-test.params \
  -p IMAGE_REGISTRY=registry.example.com/enrich-europeana \
  -p IMAGE_NAME=dei \
  -p IMAGE_TAG=build-12 \
  -n dei-staging | oc apply -f - -n dei-staging
```

### 2. Direct CLI Deployment (Production)

```bash
oc process -f okd/template.yaml --param-file=okd/env-prod.params \
  -p IMAGE_REGISTRY=registry.example.com/enrich-europeana \
  -p IMAGE_NAME=dei \
  -p IMAGE_TAG=build-12 \
  -n dei-production | oc apply -f - -n dei-production
```

---

## CI/CD Deployment with Jenkins (`build-scripts/Jenkinsfile-okd`)

A declarative pipeline is configured in `build-scripts/Jenkinsfile-okd`. It supports parameterized builds:

1. **Job Parameters:**
   - `ENVIRONMENT` (Choice: `test`, `prod`): Target deployment environment. Automatically selects target namespace (`dei-staging` vs `dei-production`), OKD server, and parameter file.
   - `BUILD_IMAGE` (Boolean, default `true`): Whether to build and push the container image to the registry before deploying. Set to `false` when deploying an existing image tag.
   - `IMAGE_TAG` (String, default `''`): Tag of the image. Defaults to `build-${BUILD_NUMBER}`.
   - `IMAGE_REGISTRY` (String, default `registry.example.com/enrich-europeana`): Registry host and namespace.
   - `IMAGE_NAME` (String, default `dei`): Repository name in registry.
   - `REGISTRY_CREDENTIALS_ID` (String, default `container-registry-creds`): Jenkins credentials ID for robot account in container registry.
   - `OKD_CREDENTIALS_ID` (String, default `''`): Optional override for Jenkins credentials ID (Secret text) for OKD API token (defaults to `okd-token` for test, `okd-token-production` for prod).
   - `OKD_SERVER` (String, default `''`): Optional override for OKD API Server URL (leave empty to resolve from `env-*-private.params` in private GitLab repository).
   - `USE_PRIVATE_CONFIG` (Boolean, default `true`): Whether to fetch and apply private parameters from GitLab.
   - `CONFIG_PROTOCOL` (Choice: `HTTPS`, `SSH`, default `HTTPS`): Protocol to fetch private configuration repository from GitLab.
   - `CONFIG_DEPLOY_TOKEN_CREDS_ID` (String, default `gitlab-config-deploy-token`): Jenkins credentials ID for GitLab Deploy Token (used when `CONFIG_PROTOCOL` is `HTTPS`).
   - `CONFIG_SSH_KEY_CREDS_ID` (String, default `gitlab-config-repo-ssh-key`): Jenkins credentials ID for GitLab SSH Deploy Key (used when `CONFIG_PROTOCOL` is `SSH`).
   - `CONFIG_REPO_URL_CREDS_ID` (String, default `''`): Optional override for Jenkins credentials ID (Secret text) containing GitLab repo URL (defaults to `gitlab-config-repo-https-url` for HTTPS or `gitlab-config-repo-url` for SSH).

2. **Pipeline Execution:**
   - Authenticates to container registry using `container-registry-creds` and builds/pushes the Docker image.
   - Fetches private environment parameter files (`env-test-private.params` / `env-prod-private.params`) from GitLab into `.private-config/`.
   - Authenticates to OKD using `okd-token` (test) or `okd-token-production` (prod).
   - Processes `okd/template.yaml` combining base `okd/env-${ENVIRONMENT}.params` with private parameters and overrides.
   - Applies resources and waits for rollout to complete.
   - Cleans up downloaded private configuration files in `post` action.

---

## Verification

```bash
# Check status of pods, pvc, service, and route in staging
oc get pods,pvc,svc,route -n dei-staging

# Tail application startup logs
oc logs -f deployment/dei-staging -c dei-app -n dei-staging
```
