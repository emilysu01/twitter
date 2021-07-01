package com.codepath.apps.restclienttemplate.models;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Entity {

    public List<String> hashtags;
    public List<String> urls;
    public List<String> userMentions;
    public List<String> symbols;

    // Empty constructor needed by the Parceler library
    public Entity() {

    }

}
