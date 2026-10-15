-- Change of the test IIIF server host: rhus-148.man.poznan.pl -> iiif-test.man.poznan.pl
-- Run ONLY on the test database (dei_ee_test). Production uses rhus-209.man.poznan.pl and must not be touched.
-- Only the host name is replaced, so all URL variants are covered
-- (with and without "https://", as well as JSON-escaped "https:\/\/").

UPDATE record
SET iiif_manifest = REPLACE(iiif_manifest, 'rhus-148.man.poznan.pl', 'iiif-test.man.poznan.pl')
WHERE iiif_manifest LIKE '%rhus-148.man.poznan.pl%';

UPDATE iiifmapping
SET iiif_resource_url = REPLACE(iiif_resource_url, 'rhus-148.man.poznan.pl', 'iiif-test.man.poznan.pl')
WHERE iiif_resource_url LIKE '%rhus-148.man.poznan.pl%';

UPDATE transcription
SET transcription_content = REPLACE(transcription_content, 'rhus-148.man.poznan.pl', 'iiif-test.man.poznan.pl')
WHERE transcription_content LIKE '%rhus-148.man.poznan.pl%';
