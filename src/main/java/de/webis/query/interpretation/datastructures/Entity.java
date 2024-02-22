package de.webis.query.interpretation.datastructures;

import org.apache.commons.io.FilenameUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Entity {
    private int begin;
    private int end;
    private String mention;

    private String url;

    private double score;

    public Entity() {

    }

    public Entity(int begin, int end, String mention, String url) {
        this.begin = begin;
        this.end = end;
        this.mention = mention;
        this.url = url;

    }

    public Entity(int begin, int end, String mention, String url, double score) {
        this.begin = begin;
        this.end = end;
        this.mention = mention;
        this.url = url;
        this.score = score;
    }

    @Override
    public String toString(){
        String encodedBaseName;
        try {
            URL urlObj = new URL(url);
            encodedBaseName = URLEncoder.encode(
                    FilenameUtils.getBaseName(urlObj.getPath()),
                    StandardCharsets.UTF_8);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String url = "https://en.wikipedia.org/wiki/" + encodedBaseName;
        return String.format("%4d | %4d | %30s | %100s | %2.4f", begin, end, mention, url, score);
    }

    @Override
    public int hashCode(){
        if(!hasUrl()){
            return mention.hashCode();
        }

        try {
            URL urlObj = new URL(url);
            String encodedBaseName = URLDecoder.decode(
                    FilenameUtils.getBaseName(urlObj.getPath()).toLowerCase(),
                    StandardCharsets.UTF_8);

            return encodedBaseName.hashCode();
        } catch (MalformedURLException | IllegalArgumentException e) {
            return url.hashCode();
        }
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof Entity) {
            return this.hashCode() == other.hashCode();
        }

        return false;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public String getMention() {
        return mention;
    }

    public String getUrl() {
        if (!hasUrl()){
            return null;
        }

        return url;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean hasUrl() {
        if (this.url == null) {
            return false;
        }

        return !url.isBlank();
    }
}
