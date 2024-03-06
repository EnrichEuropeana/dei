package pl.psnc.dei.controllers.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManifestRecreationResponse {
    long recordsCount;
    long recordsWithManifest;
    long recreatedManifests;
}
