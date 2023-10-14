<h3> Token Pass</h3>
My algorithm for implementing the token pass application first involves opening TCP talkers and listeners from one peer to each other. I did this by creating an outgoing port table, which is a 5 x 5 matrix designating ports for each peer to connect to all others. Each peer creates the same table and then starts its respective listeners and talkers. Ports are designated from one peer to itself only for convenience, and no channels are opened. Talkers are this peers number’s row and the connected peer numbers column. Listeners are the opposite.

Next, talker and listener threads are created for each communication channel. Each peer will have 4 outgoing connections and 4 incoming connections. In the main while loop, markers are sent on a seperate thread so it can happen concurrently with the token passing. This means ths each peer will have up to 10 threads running at a given time.

Each peer sets up a StateValue object to share information between talkers, listeners and main. When a listener receives the token, it activates the `state.hasToken` boolean. When this boolean is activated, an if statement in main’s while loop is triggered. Main follows to sleep for the designated token delay, and then activates a boolean `sendToken` on this peer’s talker to its successor. This will then trigger an if statement in the talker while loop, causing it to immediately  deactivate the `state.hasToken` field and its own `sendToken` field. This ensures that only one token pass message is ever sent peer receiving the token. The talker sends a token message on this channel and the process repeats.

<h3> Single Snapshot</h3>
The chandy lamport algorithm starts by checking if a peer is given a -s flag. If a peer is designated to start a snapshot and its state has reached the requested level, it will enter the start snapshot if statement of main. From here, the peer will send markers on all of its outgoing channels and begin recording on each of its listening channels. No channels will be closed at this time. When other peers receive this first marker, it will first close the channel it received the marker on. The listener will activate booleans in the shared state object `state.sendOnOthers`, `state.recordOnOthers` and `state.receivedMarker`. This will trigger an if statement in the main while loop that sets all listeners to start recording and tells every talker to send a marker.

If a listener has been told to start recording and the channel is not closed, it will add all token pass messages it receives to its recorded queue. When a listener receives a marker, it will close its channel, stop recording messages and print out its queue. Main will check in its while loop if every listener is closed and if the `state.recordOnOthers` boolean is activated, serving as an indicator of if a snapshot is happening or not. If every channel has been closed, the peer will print its snapshot over message and send no more markers. 

<h3> Testcase 2</h3>
Test case two has tokens and markers propagated with the same delay and starts immediately. The snapshot often finishes after two rounds of markers are sent so usually one token is passed, and this pass will be included in a queue. 

<h3> Testcase 3</h3>
A delayed snapshot is handled by checking if the given delay is equal to the current state. In this case, the token propagates slower than the marker, meaning a token pass will be recorded in the queue  

<h3> Testcase 4 </h3>
Consecutive snapshots are handled by ensuring that each snapshot ends. Because TCP guarantees messages will be delivered, markers sent must be eventually received. So, because all markers are sent we know the all channels will eventually close and the snapshot will end. I planned to have an incrementing index for markers on consecutive snapshots, but did not finish this approach and had the algorithm worked without adding this index. The specifics of this are described for the next testcase.

<h3> Testcase 5 </h3>
I did not finish my implementation of concurrent snapshots and will describe my algorithm below: 
To handle concurrent snapshots, I planned to append a marker index to the marker message. I would redesign many of my shared booleans to become arraylists of booleans. When a peer begins a snapshot, it will check its `state.currentMarker` index. It will send its 4 markers with the index appended to it. After sending these markers, the `state.currentMarker` will increment. When a peer receives a first marker of a new index, it will also increment its `state.currentMarker` field.

Next, state.receivedMarker would become an array list of receivedMarkers, each index corresponding to a marker index (both start at 0).  When main goes to send markers, it’ll iterate through this array. It will start at its marker index, and if the boolean in this array tells it to send, it will send out 4 markers with that index. This will send all markers of the corresponding index only when they receive the first marker. Markers of a certain index will only be sent from each peer once.

listener.isClosed would also become an array of booleans, each indicating  if a listener is closed to a marker of a corresponding index.

When main checks if a snapshot is over, it will start at the received marker array. At each index, it will check if all listeners are closed for that specific marker index, and if so, print the snapshot complete message.

When a listener receives a marker, it will parse the index and process it according to the index it receives. The logic will stay the same, except receiving a marker of index `i` will update fields :
```
* state.sendOnOthers[i] = true;
* state.recordOnOthers[i] = true;
* this.isRecording [i]= false;
* this.state.receivedMarker[i] = true;
* this.isClosed[i] = true
```

This is very similar to how the listener currently works, and will just need to make data structure changes and debug. 


