package ru.ladybug.isolatedsingularity.net;

import ru.ladybug.isolatedsingularity.LocalState;

public interface StatefulActivity {
    LocalState getState();
}
