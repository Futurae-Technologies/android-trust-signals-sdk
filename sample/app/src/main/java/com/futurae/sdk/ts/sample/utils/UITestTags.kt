package com.futurae.sdk.ts.sample.utils

enum class UITestTags(val tag: String) {
    // Home screen — actions
    CollectNowButton("collect_now"),
    CollectAndUploadButton("collect_and_upload"),

    // Home screen — inputs
    AccountIDInput("accountid_input"),
    AccessTokenInput("access_token_input"),
    ServiceIDInput("serviceid_input"),
    UnitIDInput("unitid_input"),
    InteractionIDInput("interactionid_input"),

    // Home screen — verification status picker (tag on the whole row)
    VerificationStatusOption("verification_status_option"),    // individual buttons use "${VerificationStatusOption.tag}_${status.name.lowercase()}", e.g. verification_status_option_verified

    // Home screen — frequency picker (tag on the whole row)
    FrequencyOption("frequency_option"),    // individual buttons use "${FrequencyOption.tag}_${frequency.name.lowercase()}", e.g. frequency_option_off

    // Home screen — collections list
    CollectionsCount("collections_count"),
    CollectionEmptyState("collection_empty_state"),
    CollectionList("collection_list"),
    CollectionItem("collection_item"),          // used with "_$index" suffix, e.g. collection_item_0

    // Detail screen — navigation
    CollectionDetailBackButton("collection_detail_back"),

    // Detail screen — raw JSON
    CopyJsonButton("copy_json"),
    RawJsonText("raw_json_text"),
}
