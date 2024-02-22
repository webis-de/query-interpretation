FROM maven:3.8.3-adoptopenjdk-15
LABEL authors="Marcel Gohsen"

COPY src /query-interpretation/src
COPY pom.xml /query-interpretation/pom.xml
COPY lib /query-interpretation/lib

COPY data/persistent/embeddings /query-interpretation/data/persistent/embeddings
COPY data/persistent/lucene-entity-index /query-interpretation/data/persistent/lucene-entity-index
COPY data/persistent/ngrams /query-interpretation/data/persistent/ngrams
COPY data/persistent/entity-commonness /query-interpretation/data/persistent/entity-commonness
COPY data/persistent/wiki-entity-index /query-interpretation/data/persistent/wiki-entity-index

WORKDIR /query-interpretation

RUN mvn install:install-file -Dfile=lib/query-segmentation-application-1.0-jar-with-dependencies.jar -DgroupId=de.webis.query-segmentation -DartifactId=query-segmentation-application -Dversion=1.0 -Dpackaging=jar && mvn clean package


