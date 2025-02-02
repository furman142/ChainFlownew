package com.example.chain_flow.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chain_flow.models.CryptoCoin

class CryptoViewModel:ViewModel()
{
    val cryptoList = MutableLiveData<List<CryptoCoin>>()

}