// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.androidApplication) apply false
}
true // Needed to make the Suppress annotation work for the plugins block