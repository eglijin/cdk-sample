package ap

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

val blocker: Int = runBlocking { delay(20000); 20000; }