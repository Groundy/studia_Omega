package com.example.omega
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class ApiFuncs {
	companion object{
		fun generate_X_REQUEST_ID() : UUID{
			val random = Random()
			val random63BitLong = random.nextLong() and 0x3FFFFFFFFFFFFFFFL
			val variant1BitFlag : Long = (-0x6000000000000000L).toLong()
			val least64SigBits = random63BitLong + variant1BitFlag

			val start: LocalDateTime = LocalDateTime.of(1582, 10, 15, 0, 0, 0)
			val duration: Duration = Duration.between(start, LocalDateTime.now())
			val timeForUuidIn100Nanos = duration.seconds * 10000000 + duration.nano * 100
			val least12SignificantBitOfTime = timeForUuidIn100Nanos and 0x000000000000FFFFL shr 4
			val version = (1 shl 12).toLong()
			val most64SigBits = (timeForUuidIn100Nanos and -0x10000L) + version + least12SignificantBitOfTime


			val uuid = UUID(most64SigBits, least64SigBits)
			return uuid
		}
	}

}