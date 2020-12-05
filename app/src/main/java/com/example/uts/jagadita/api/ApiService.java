package com.example.uts.jagadita.api;

import com.example.uts.jagadita.models.ApiResponse;
import com.example.uts.jagadita.models.Pengguna;
import com.example.uts.jagadita.models.Perusahaan;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @POST("login.php")
    @FormUrlEncoded
    Call<Pengguna> login(@Field("username") String username, @Field("password") String password);

    @POST("register.php")
    Call<ApiResponse> register(@Body Pengguna user);

    @POST("update_profil.php")
    Call<ApiResponse> update_profil(@Body Pengguna user);

    @POST("create_usaha.php")
    Call<ApiResponse> daftar_usaha(@Body Perusahaan usaha);

    @POST("get_usaha.php")
    @FormUrlEncoded
    Observable<List<Perusahaan>> get_usaha(@Field("id_pemilik") int id_pemilik);

    @POST("get_usaha_all.php")
    Observable<List<Perusahaan>> get_usaha_all();
}