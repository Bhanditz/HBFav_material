package me.rei_m.hbfavmaterial.events.network

import me.rei_m.hbfavmaterial.entities.HatenaRestApiBookmarkResponse

public class HatenaPostBookmarkLoadedEvent(val response: HatenaRestApiBookmarkResponse?,
                                           val status: LoadedEventStatus)
