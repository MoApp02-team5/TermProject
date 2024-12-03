package com.example.termproject.ui.home

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {
    private val _state = MutableStateFlow<SignOutState>(SignOutState.Nothing)
    val state = _state.asStateFlow()

    fun signOut(){
        FirebaseAuth.getInstance().signOut()
        _state.value = SignOutState.LoggedOut
    }
}

sealed class SignOutState{
    object Nothing: SignOutState()
    object LoggedOut: SignOutState()
}