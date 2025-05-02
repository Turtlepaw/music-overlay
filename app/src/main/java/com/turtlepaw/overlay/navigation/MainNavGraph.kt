package com.turtlepaw.overlay.navigation

import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility

@NavHostGraph(
    defaultTransitions = DefaultFadingTransitions::class,
    route = "preferred_route",
    visibility = CodeGenVisibility.INTERNAL,
)
annotation class MainGraph