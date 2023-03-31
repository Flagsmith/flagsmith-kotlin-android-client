# prevent proguard / R8 from obfuscating entity fields
-keep class com.flagsmith.entities.** {
    <fields>;
}
