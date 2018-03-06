package com.project.ken.vecurityguard.Remote;

import com.project.ken.vecurityguard.Models.FCMResponse;
import com.project.ken.vecurityguard.Models.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by ken on 3/6/18.
 */

public interface IFCMService {
    @Headers({
            "Content-Type: application/json",
            "Authorization:key=AAAAXVi46gc:APA91bEMHHxrVm8EikPAqxc3FYdnMEYqlS-btXaq2HIt6I8qvlSncZ8qUo2eqlixrZ_gexI-J96WC2mNmM3Qeep7YnibgTY9PLpDVlOf9Zm02u7-gmHApebF6I5Z2fsc5pj9KzTXNGGw"
    })

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
