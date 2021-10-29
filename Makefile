.PHONY: build serve deploy

build:
	soupault

serve:
	python3 -m http.server --directory  _site

deploy:
	aws s3 sync --exclude "artifacts/*/*.js" _site/ s3://dbp.io
	aws s3 sync --exclude "*" --include "artifacts/*/*.js" --content-type "application/javascript" --content-encoding "gzip" _site/ s3://dbp.io
