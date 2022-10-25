package com.flagsmith.interfaces

import com.flagsmith.response.TraitWithIdentity

interface ITraitUpdateResult {
    fun success(response: TraitWithIdentity);
    fun failed(exception: Exception );
}
