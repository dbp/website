.PHONY: build serve deploy

build:
	soupault

serve:
	python3 -m http.server --directory  _site

deploy: build
	aws s3 sync --size-only --exclude "artifacts/*/*.js" _site/ s3://dbp.io
	aws s3 sync --size-only --exclude "*" --include "artifacts/*/*.js" --content-type "application/javascript" --content-encoding "gzip" _site/ s3://dbp.io
