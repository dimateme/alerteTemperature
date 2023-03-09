package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class MainActivityViewModel :ViewModel() {
    lateinit var createNewTemperatureLiveData: MutableLiveData<TemperatureResponse?>
    init {
        createNewTemperatureLiveData = MutableLiveData()
    }
    fun getCreateNewTemperatureObserver(): MutableLiveData<TemperatureResponse?>{
        return createNewTemperatureLiveData
    }
    fun createNewTemperature(temperature: Temperature){
        val retroService = RetroInstance.getRetroInstance().create(RetroService::class.java)
        val call = retroService.ajouterTemperature(temperature)
        call.enqueue((object :Callback<TemperatureResponse>{
            override fun onFailure(call: Call<TemperatureResponse>, t: Throwable) {
                createNewTemperatureLiveData.postValue(null)
            }

            override fun onResponse(call: Call<TemperatureResponse>, response: Response<TemperatureResponse>) {
                if(response.isSuccessful){
                    createNewTemperatureLiveData.postValue(response.body())
                }else{
                    createNewTemperatureLiveData.postValue(null)
                }
            }
        }))
    }
}