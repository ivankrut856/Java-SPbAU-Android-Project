package ru.ladybug.isolatedsingularity.net;

import ru.ladybug.isolatedsingularity.LocalState;

/** Interface represents activity with associated state */
public interface StatefulActivity {
    LocalState getState();
}
