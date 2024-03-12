@Composable
internal fun AppTray(
    context: AppContext,
    showApp: () -> Unit,
    closeApp: () -> Unit
) {
    val state: TrayState = rememberTrayState()

    Tray(
        state = state,
        icon = AppTrayIcon,
        tooltip = "SpMp // TODO",
        menu = {
            Item(
                "Exit // TODO",
                onClick = showApp
            )
        }
    )
}

private object AppTrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}
