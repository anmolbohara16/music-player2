# Existing project audit

Single Android app, `com.example`, Kotlin/Compose, AGP 9.1.1. MainActivity requests media/notification permission; MainScreen uses StateFlow navigation and modal components. MusicViewModel coordinates MusicRepository (MediaStore plus Room override records) and MusicPlayerController. Playback is Android MediaPlayer, a foreground MusicPlaybackService and platform MediaSession, not Media3. Keep this architecture.

Room v4 stores favorites, history, playlists/membership, settings, excluded songs, explicit play counts, and nullable metadata overrides. Scanner reads MediaStore and excludes voice/system audio. Coil handles artwork. Lyrics are stored as plain/synchronized text. No audio tag writing exists.

Existing screens: Home, Songs, Albums/detail, Artists/detail, Playlists/detail, Favorites, Search; untracked ProfileScreen includes library/settings and was not wired. Existing sheets/dialogs: now playing, queue, player options, song info, metadata editor, metadata comparison, identify link, lyrics, equalizer, sleep timer, playlist creation/add, sort, deletion. Theme is hardcoded dark glass; no functional light mode.

Metadata stopped at provider/service scaffolding and ViewModel methods. iTunes and MusicBrainz search, YouTube/Spotify oEmbed, LRCLIB and review/editor components exist. MainScreen does not display metadata dialogs or pass required options callbacks. Songs accepts callbacks without forwarding them. Batch is a service stub only. Matching strips some versions, accepts substring similarity, and ignores large duration mismatches. Link resolution takes the first iTunes hit and promotes it to HIGH. LRCLIB fallback accepts the first result. Network errors become no-match. Updates overwrite unselected disc/IDs, erase manual flags, and can replace useful fields with blanks.

Other audit findings: repository injects fabricated sample tracks into every library; profile estimates listening time from full track lengths; initial tests reference removed Greeting and missing Song album argument; debug signing refers to absent debug.keystore; database uses destructive fallback. Artwork list component uses subcomposition. Queue metadata was not refreshed from the repository. No metadata or DB tests exist at baseline.

Implementation order: safe provider matching and errors; transactional merge policy; link/review and batch UI; shared adaptive lavender/plum design and existing screen improvements; compile, matching/provider/DB tests and rendered UI checks where available.
