package com.enach.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enach.client.data.models.TokenResponse
import com.enach.client.data.repository.AuthTokenManager
import com.enach.client.data.repository.ENachRepository
import com.enach.client.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: ENachRepository,
    private val tokenManager: AuthTokenManager
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<Resource<TokenResponse>?>(null)
    val loginState: StateFlow<Resource<TokenResponse>?> = _loginState.asStateFlow()
    
    private val _registerState = MutableStateFlow<Resource<TokenResponse>?>(null)
    val registerState: StateFlow<Resource<TokenResponse>?> = _registerState.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        _isAuthenticated.value = tokenManager.isAuthenticated()
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            repository.login(username, password).collect { result ->
                _loginState.value = result
                if (result is Resource.Success) {
                    _isAuthenticated.value = true
                    tokenManager.saveUserInfo(username, "")
                }
            }
        }
    }
    
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(username, email, password).collect { result ->
                _registerState.value = result
                if (result is Resource.Success) {
                    _isAuthenticated.value = true
                    tokenManager.saveUserInfo(username, email)
                }
            }
        }
    }
    
    fun logout() {
        tokenManager.clearAuth()
        _isAuthenticated.value = false
        _loginState.value = null
        _registerState.value = null
    }
    
    fun clearLoginState() {
        _loginState.value = null
    }
    
    fun clearRegisterState() {
        _registerState.value = null
    }
}
