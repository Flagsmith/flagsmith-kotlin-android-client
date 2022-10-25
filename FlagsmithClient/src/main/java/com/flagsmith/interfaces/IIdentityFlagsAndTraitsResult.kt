package com.flagsmith.interfaces

import com.flagsmith.response.IdentityFlagsAndTraits

interface IIdentityFlagsAndTraitsResult {
    fun success(response: IdentityFlagsAndTraits)
    fun failed(e: Exception)
}
