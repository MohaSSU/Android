package com.example.mohassu;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserProfileViewModel extends ViewModel {
    private final MutableLiveData<String> nickname = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> birthDate = new MutableLiveData<>();
    private final MutableLiveData<Uri> photoUri = new MutableLiveData<>();

    public void setNickname(String nickname) {
        this.nickname.setValue(nickname);
    }

    public LiveData<String> getNickname() {
        return nickname;
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public LiveData<String> getName() {
        return name;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate.setValue(birthDate);
    }

    public LiveData<String> getBirthDate() {
        return birthDate;
    }

    public void setPhotoUri(Uri uri) {
        this.photoUri.setValue(uri);
    }

    public LiveData<Uri> getPhotoUri() {
        return photoUri;
    }

}
