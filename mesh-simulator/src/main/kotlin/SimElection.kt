enum class NodeRole { FOLLOWER, LEADER, IN_ELECTION }

data class ElectionState(
    var leader: Int = -1,
    var role: NodeRole = NodeRole.FOLLOWER,
    var lastHeartbeat: Long = 0L
)