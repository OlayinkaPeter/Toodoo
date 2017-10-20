package com.olayinkapeter.toodoo.models

/**
 * Created by Olayinka_Peter on 10/3/2017.
 */
class UserClass {
    var name: String = ""
    var email: String = ""

    // Default constructor required for calls to
    // DataSnapshot.getValue(UserModel.class)
    constructor() {}

    constructor(name: String, email: String) {
        this.name = name
        this.email = email
    }
}
