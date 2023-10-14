
I faced significant challenges writing this program, mostly with the timing of messages and understanding the algorithm properly. I made an initial mistake on Thursday where I only sent markers to neighbors, rather than every other peer. Upon realizing this, I had to completely refactor my main. This proved troublesome, as it took me until late Friday night to reimplement token pass. Once I finished token pass,  I ran into many issues with my timing, and found that arbitrary changes in `sleep` statements could drastically impact the correctness of my code.  There are several sleeps in my code, after setting up listens, talkers, and at the end of each while loop iteration that keeps my algorithm from behaving unexpectedly.

My previous implementation can be found on the main branch of my github repo.
https://github.com/isadhwani/chaundy-lamport/blob/main/. This version is on branch final

My biggest concern is with the sleeping, as it does feel like the time is not always consistent. I have the processses sleep in main before activating the talker to send talkers and markers, but sometimes it seems to ignore the sleep. This makes the expected queues a little unpredictable.

I could not finish the concurrent snapshots by the deadline, but I outlined my approach. I beleive I described my algorithm well and have a strong belief that my algorithm is sound

