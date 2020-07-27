package com.example.chatapp.Fragments;

import com.example.chatapp.Notifications.MyResponse;
import com.example.chatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService
{
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAp5u0Sis:APA91bFthvYJGAxER9N38YQhVTvdPUjMyuudejClwch6PKbFGr9H_ehecACQEz6ifEJ8p5kb1wFTVIjtfGCM1WrZR0DkA5OFfhkTegvRNxDew3Wigh1j_-9bmDviDe-vnC2MBHrlaMPx"

    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
