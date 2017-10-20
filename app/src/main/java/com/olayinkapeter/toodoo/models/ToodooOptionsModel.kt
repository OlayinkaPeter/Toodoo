package com.olayinkapeter.toodoo.models

/**
 * Created by Olayinka_Peter on 10/3/2017.
 */

class ToodooOptionsModel {
    var optionIcon: Int = 0
    var optionTitle: String? = null
    var optionValue: String? = null

    constructor() {}

    constructor(optionIcon: Int, optionTitle: String, optionValue: String) {
        this.optionIcon = optionIcon
        this.optionTitle = optionTitle
        this.optionValue = optionValue
    }
}