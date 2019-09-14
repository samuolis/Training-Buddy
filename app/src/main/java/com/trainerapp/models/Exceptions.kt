package com.trainerapp.models

class Exceptions {

    class PermissionsDenied : RuntimeException()
    class PermissionsDeniedForever : RuntimeException()
}
