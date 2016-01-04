package me.rei_m.hbfavmaterial.entities

import java.io.Serializable

/**
 * TwitterSessionのEntity.
 */
public data class TwitterSessionEntity(var userId: Long = 0,
                                       var userName: String = "",
                                       var oAuthTokenEntity: OAuthTokenEntity) : Serializable
