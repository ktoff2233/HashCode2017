package main.java;


import java.util.*;
public class InputData{
    int num_videos;
    int num_endpoints;
    int num_requests;
    int num_caches;
    int cache_size;
    int[] video_size_desc;
    List<Integer> ep_to_dc_latency;
    List<List<Integer>> ep_to_cache_latency;
    List<List<Integer>> ed_cache_list;
    Map<String, String> video_ed_request;
    
    public InputData(){
        num_videos = 0;
        num_endpoints = 0;
        num_requests = 0;
        num_caches = 0;
        cache_size = 0;
        video_size_desc = null;
        ep_to_dc_latency = new ArrayList<Integer>();
        ep_to_cache_latency = new ArrayList<List<Integer>>();
        ed_cache_list =  new ArrayList<List<Integer>>();;
        video_ed_request = new HashMap<String, String>();
        
    }
    
}

