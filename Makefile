IMAGE_NAME=registry.webis.de/code-lib/public-images/query-interpretation
VERSION=1.0

SHELL := /bin/bash

clean:
	mvn clean
	rm -rf venv data

download:
	/bin/bash ./src/main/sh/install-el.sh
	/bin/bash ./src/main/sh/install-interpretation.sh

build: download
	docker build -t ${IMAGE_NAME}:${VERSION} .

deploy: build
	docker push ${IMAGE_NAME}:${VERSION}

tira:
	python3 -m venv venv
	source venv/bin/activate && pip install tira
dev-bash:
	docker run --rm -ti -v ${PWD}/data:/data --entrypoint bash ${IMAGE_NAME}:${VERSION}

tira-run: deploy tira
	source venv/bin/activate && tira-run \
		--image ${IMAGE_NAME}:${VERSION} \
		--input-dir data/${CORPUS}/ \
		--output-dir data/${CORPUS}-cache/ \
		--command 'java -jar /query-interpretation/target/query-interpretation-1.0-jar-with-dependencies.jar --input $$inputDataset --cache $$outputDir --output $$outputDir'
