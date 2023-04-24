package com.example.Lab7_MonoApi;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface JSONPlaceHolderApi {
    @GET("currency")
    Call<List<CurrencyPojo>> getAllCurrencies();
}
