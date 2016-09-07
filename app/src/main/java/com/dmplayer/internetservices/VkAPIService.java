package com.dmplayer.internetservices;

import com.dmplayer.models.VkObjects.VkAlbumsResponse.VkAlbumsWrapper;
import com.dmplayer.models.VkObjects.VkAudioGetResponce.VkAudioWrapper;
import com.dmplayer.models.VkObjects.VkPopularAudioResponce.VkPopularCollection;
import com.dmplayer.models.VkObjects.VkProfileUserDataResponse.VkUserDataCollection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VkAPIService {
        @GET("users.get?v=5.53")
        Call<VkUserDataCollection> loadUserData(@Query("fields") String fieldList,
                                                @Query("access_token") String token);

        @GET("audio.getCount?v=5.53")
        Call<ResponseBody> loadSongsCount(@Query("owner_id") String userId,
                                          @Query("access_token") String token);

        @GET("audio.getAlbums?v=5.53")
        Call<VkAlbumsWrapper> loadAlbums(@Query("offset") String offset,
                                         @Query("count") String count,
                                         @Query("owner_id") String userId,
                                         @Query("access_token") String token);

        @GET("audio.get?need_user=0&v=5.53")
        Call<VkAudioWrapper> loadAudio(@Query("album_id") String albumId,
                                       @Query("offset") String offset,
                                       @Query("count") String count,
                                       @Query("owner_id") String userId,
                                       @Query("access_token") String token);

        @GET("audio.getPopular?only_eng=1&v=5.53")
        Call<VkPopularCollection> loadPopularAudio(@Query("offset") String offset,
                                                   @Query("count") String count,
                                                   @Query("access_token") String token);

        @GET("audio.getRecommendations?shuffle=1&v=5.53")
        Call<VkAudioWrapper> loadRecommendedAudio(@Query("offset") String offset,
                                                  @Query("count") String count,
                                                  @Query("owner_id") String userId,
                                                  @Query("access_token") String token);
}