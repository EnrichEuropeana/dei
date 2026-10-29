# DEI Deployment on OKD / OpenShift

This directory contains manifests and OpenShift Templates for deploying the **DEI** (Data Exchange Infrastructure) application on OKD / OpenShift.

---

## Files Overview

- `template.yaml` – **Recommended:** OpenShift Template (`kind: Template`) combining deployment resources (PVC, ConfigMap, Deployment, Service, Route) with parameter support for multiple environments (`test`, `prod`). Sensitive secrets are referenced via `${APP_NAME}-secret` and managed separately directly in OKD.
- `env-test.params` – Non-sensitive parameter values for the test / staging environment (`enrich-europeana-dei-staging`).
- `env-prod.params` – Non-sensitive parameter values for the production environment (`enrich-europeana-dei-production`).
- `01-pvc.yaml` – `PersistentVolumeClaim` (`dei-conversion-pvc`) providing persistent storage for temporary conversion files (mounted at `/data/conversion`).
- `02-configmap.yaml` – `ConfigMap` (`dei-config`) containing test environment configurations and external service endpoints.
- `03-secret.yaml.example` – Example `Secret` definition for sensitive credentials (database credentials and API tokens). To be applied manually or created in OKD Console.
- `04-deployment.yaml` – Application `Deployment` configured with `Recreate` strategy and health probes.
- `05-service.yaml` – Internal cluster `Service` (`ClusterIP`).
- `06-route.yaml` – OpenShift `Route` with SSL termination (`edge`).
- `dei-all.yaml` – Consolidated static manifest containing individual non-sensitive objects.

---

## Deployment Strategy & Principles

1. **Environment-Agnostic Image**: The container image built by Docker contains pre-compiled Vaadin assets and application code. It is independent of whether it is deployed to test or production. The runtime behavior and configuration are determined via `SPRING_PROFILES_ACTIVE` along with environment variables injected via OKD ConfigMaps and Secrets.
2. **Neutral Image Tagging**: Images pushed to the container registry use versioned or build-based tags (e.g. `build-42` or `1.0.0`) without embedding environment names (such as `test` or `prod`). This ensures that the exact same tested image artifact can be promoted and deployed to production.
3. **Environment Separation**:
   - **Test / Staging**: Namespace `enrich-europeana-dei-staging` on OKD cluster `https://bst2.paas.psnc.pl`.
   - **Production**: Namespace `enrich-europeana-dei-production` on OKD cluster `https://bst2.paas.psnc.pl`.
4. **Secret & Credential Management**:
   - Application secrets (`${APP_NAME}-secret`) are maintained directly in OKD (via OKD Web Console or `oc create secret generic`). They are **not** committed to Git and will not be overwritten by CI/CD pipeline deploys.
   - Example command to create application secret:
     ```bash
     # Staging environment
     oc create secret generic dei-staging-secret \
       --from-literal=SPRING_DATASOURCE_URL="jdbc:mysql://mysql-db1.man.poznan.pl:3307/dei_ee_test?useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false" \
       --from-literal=SPRING_DATASOURCE_USERNAME="dei_ee" \
       --from-literal=SPRING_DATASOURCE_PASSWORD="db-password" \
       --from-literal=EUROPEANA_NEW_API_TP_AUTHORIZATION_TOKEN="token" \
       --from-literal=API_CLIENT_ID="transcribathon-dei" \
       --from-literal=API_CLIENT_SECRET="client-secret" \
       --from-literal=API_USERNAME="api-username" \
       --from-literal=API_PASSWORD="api-password" \
       --from-literal=DRI_API_TOKEN="dri-token" \
       -n enrich-europeana-dei-staging

     # Production environment
     oc create secret generic dei-production-secret \
       --from-literal=SPRING_DATASOURCE_URL="jdbc:mysql://mysql-db1.man.poznan.pl:3307/dei_ee?useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false" \
       --from-literal=SPRING_DATASOURCE_USERNAME="dei_ee" \
       --from-literal=SPRING_DATASOURCE_PASSWORD="production-db-password" \
       --from-literal=EUROPEANA_NEW_API_TP_AUTHORIZATION_TOKEN="production-token" \
       --from-literal=API_CLIENT_ID="production-client-id" \
       --from-literal=API_CLIENT_SECRET="production-client-secret" \
       --from-literal=API_USERNAME="production-username" \
       --from-literal=API_PASSWORD="production-password" \
       --from-literal=DRI_API_TOKEN="production-dri-token" \
       -n enrich-europeana-dei-production
     ```
   - OKD token is stored in Jenkins Credentials as Secret Text (`okd-token`).
   - Harbor registry credentials are stored in Jenkins Credentials as Username/Password (`harbor-creds`).

---

## Deployment via OpenShift Template (`template.yaml`)

Using `template.yaml` allows parameterizing all variables and deploying with a single command.

### 1. Direct CLI Deployment (Test / Staging)

```bash
oc process -f okd/template.yaml --param-file=okd/env-test.params \
  -p IMAGE_REGISTRY=registry.paas.psnc.pl/enrich-europeana \
  -p IMAGE_NAME=dei \
  -p IMAGE_TAG=build-12 \
  -n enrich-europeana-dei-staging | oc apply -f - -n enrich-europeana-dei-staging
```

### 2. Direct CLI Deployment (Production)

```bash
oc process -f okd/template.yaml --param-file=okd/env-prod.params \
  -p IMAGE_REGISTRY=registry.paas.psnc.pl/enrich-europeana \
  -p IMAGE_NAME=dei \
  -p IMAGE_TAG=build-12 \
  -n enrich-europeana-dei-production | oc apply -f - -n enrich-europeana-dei-production
```

---

## CI/CD Deployment with Jenkins (`build-scripts/Jenkinsfile-okd`)

A declarative pipeline is configured in `build-scripts/Jenkinsfile-okd`. It supports parameterized builds:

1. **Job Parameters:**
   - `ENVIRONMENT` (Choice: `test`, `prod`): Target deployment environment. Automatically selects target namespace (`enrich-europeana-dei-staging` vs `enrich-europeana-dei-production`), OKD server (`https://bst2.paas.psnc.pl`), and parameter file.
   - `BUILD_IMAGE` (Boolean, default `true`): Whether to build and push the container image to the registry before deploying. Set to `false` when deploying an existing image tag.
   - `IMAGE_TAG` (String, default `''`): Tag of the image. Defaults to `build-${BUILD_NUMBER}`.
   - `IMAGE_REGISTRY` (String, default `registry.paas.psnc.pl/enrich-europeana`): Registry host and namespace.
   - `IMAGE_NAME` (String, default `dei`): Repository name in registry.
   - `REGISTRY_CREDENTIALS_ID` (String, default `harbor-creds`): Jenkins credentials ID for robot account in Harbor.
   - `OKD_CREDENTIALS_ID` (String, default `okd-token`): Jenkins credentials ID for OKD API token.
   - `OKD_SERVER` (String, default `https://bst2.paas.psnc.pl`): OKD API Server URL.
   - `OKD_NAMESPACE` (String, optional): Namespace override (defaults to environment setting).

2. **Pipeline Execution:**
   - Authenticates to Harbor using `harbor-creds` and builds/pushes the Docker image.
   - Authenticates to OKD using `okd-token`.
   - Processes `okd/template.yaml` with the corresponding `okd/env-${ENVIRONMENT}.params` and overrides.
   - Applies resources and waits for rollout to complete.

---

## Verification

```bash
# Check status of pods, pvc, service, and route in staging
oc get pods,pvc,svc,route -n enrich-europeana-dei-staging

# Tail application startup logs
oc logs -f deployment/dei-staging -c dei-app -n enrich-europeana-dei-staging
```
