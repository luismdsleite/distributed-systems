# Distributed Systems
Implementation of 3 different types of distributed systems:
-   Token Ring
-   Gossip-based data dissemination
-   Totally Ordered Multicast

Details are available at [Practical Assignment](/practical_assignment_PT.pdf) (Although only in Portuguese).

# TokenRing
The tokenRing server accepts the following commands:
<ul>
<li><b>lock</b>: locks the token (as soon as it's received).
<li><b>unlock</b>: unlocks the token (as soon as it's received).
</ul>

# Gossiping

The Gossiping Server accepts the following commands:
<ul>
<li><b>register {ipv4}/unregister {ipv4}</b>: Adds/removes the host.
<li><b>send {ipv4} {msg}</b>: Sends a msg to a certain host (Used for testing
purposes).
<li><b>get all</b>: Equivalent to executing all get commands.
<li><b>get words</b>: List of the received words.
<li><b>get hosts</b>: List of all registered hosts.
<li><b>get banned</b>: List of words that are not propagated.
<li><b>get gossipRate</b>: nº of randomly chosen hosts a message is
propagated to.
<li><b>get dropChance</b>: chance of a word retransmission being ignored.
<li><b>set gossipRate</b>: changes gossipRate param (3 by default).
<li><b>set dropChance</b>: changes dropChance param.
</ul>