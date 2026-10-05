#include "cpss-nemon.h"

struct nm_sync_cluster_config nm_sync_cluster_config;
struct nm_sync_cluster_status nm_sync_cluster_status;

static void nm_sync_cluster_status_peer_count_update(void)
{
	int i;
	int count = 0;

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		if(nm_sync_cluster_status.peer_status.peer[i].valid)
			count++;
	}

	nm_sync_cluster_status.peer_status.count = count;
}

static void nm_sync_cluster_status_peer_sync(void)
{
	int i;
	struct nm_sync_cluster_peer_info *conf;
	struct nm_sync_cluster_peer_status *peer;

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		conf = &nm_sync_cluster_config.peer.list[i];
		peer = &nm_sync_cluster_status.peer_status.peer[i];

		if(!conf->valid || conf->ip.s_addr == 0) {
			memset(peer, 0, sizeof(*peer));
			continue;
		}

		if(peer->valid && peer->ip.s_addr == conf->ip.s_addr)
			continue;

		memset(peer, 0, sizeof(*peer));
		peer->valid = 1;
		peer->ip = conf->ip;
		peer->connect = NM_CLUSTER_STATE_SYNC_NEVER;
		peer->last_rx_ms = 0;
	}

	nm_sync_cluster_status_peer_count_update();
}

static struct nm_sync_cluster_peer_status *
nm_sync_cluster_peer_status_get_by_ip(uint32_t ip)
{
	int i;
	struct nm_sync_cluster_peer_status *peer;

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer = &nm_sync_cluster_status.peer_status.peer[i];

		if(!peer->valid)
			continue;

		if(peer->ip.s_addr == ip)
			return peer;
	}

	return NULL;
}

void nm_sync_cluster_save_local(void)
{
	int id;
	int tool_status;

	// Save the current local tool state once
	if(nm_cluster_state.saved)
		return;

	if(!nm_cluster_state.boot_sync_time) {
		osTimerWkAfter(5400);

		for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
			tool_status = nm_bypass_cluster_status_get(id);
			nm_cluster_state.saved_tool[id] = tool_status;
			nm_cluster_state.local_tool[id] = tool_status;
			nm_cluster_state.boot_sync_time = 1;
		}
	} else {
		for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
			tool_status = nm_bypass_cluster_status_get(id);
			nm_cluster_state.saved_tool[id] = tool_status;
			nm_cluster_state.local_tool[id] = tool_status;
		}
	}

	nm_cluster_state.saved = 1;
}

void nm_sync_cluster_recover_local(void)
{
	int id;
	int clear = 1;

	if(nm_cluster_state.saved || nm_cluster_state.fail_action_active) {
		for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
			nm_bypass_cluster_set(id,
					nm_cluster_state.saved_tool[id],
					clear);
		}
	}

	nm_cluster_state.saved = 0;
	nm_cluster_state.fail_action_active = 0;
	nm_sync_cluster_status.fail_action_active = 0;
	nm_cluster_state.fail_action_mode = -1;
}

void nm_sync_cluster_init(void)
{
	memset(&nm_cluster_state, 0, sizeof(nm_cluster_state));
	memset(&nm_sync_cluster_status, 0, sizeof(nm_sync_cluster_status));

	nm_cluster_state.fail_action_mode = -1;
	nm_sync_cluster_status.fail_action_active = 0;
	nm_sync_cluster_status.rebooting = 0;

	nm_sync_cluster_status_peer_sync();
}

//for after mesh mode
int nm_sync_cluster_peer_ip_get(int index, uint32_t *ip)
{
	struct nm_sync_cluster_peer_info *peer;

	if(!ip || index < 0 || index >= NM_SYNC_CLUSTER_PEER_MAX)
		return -1;

	peer = &nm_sync_cluster_config.peer.list[index];

	*ip = peer->valid ? peer->ip.s_addr : 0;

	return 0;
}

int nm_socket_cluster_config_set(struct nm_sync_cluster_config *config)
{
	int sock;

	if(!config)
		return -1;

	if(config->interval_ms < 100 || config->interval_ms > 10000)
		return -1;

	if(config->time_out < 500 || config->time_out > 300000)
		return -1;

	nm_sync_cluster_config = *config;
	nm_sync_cluster_status.fail_action_active =
		nm_cluster_state.fail_action_active;
	nm_sync_cluster_status_peer_sync();

	sock = nm_socket_cluster_send_open();
	if(sock >= 0) {
		nm_socket_cluster_send_packet(sock);
		close(sock);
	}

	if(nm_sync_cluster_config.enable == 0)
		nm_sync_cluster_recover_local();

	return 0;
}

int nm_sync_cluster_make_tx_data(struct nm_sync_cluster_peer_status *data)
{
	int id;
	int status = 0;
	int active = 0;
	int tool_status;
	int tool_id;
	int operate_status;
	struct system_information sys_info;

	memset(data, 0, sizeof(*data));
	memset(&sys_info, 0, sizeof(sys_info));

	data->valid = 1;

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
		tool_id = id + 1;

		if(nm_cluster_state.fail_action_active && nm_cluster_state.saved) {
			tool_status = nm_cluster_state.saved_tool[id];
		} else {
			tool_status = nm_bypass_cluster_status_get(id);
			nm_cluster_state.local_tool[id] = tool_status;
		}

		operate_status = tool_status;

		if(nm_inl_bypass_status_get(tool_id, &status, &active) == 0)
			operate_status =
				(status == NM_BYPASS_STATUS_INLINE) ? 1 : 0;

		if(tool_id == 1) {
			data->inline1_status = tool_status;
			data->inline1_operate_status = operate_status;
		} else {
			data->inline2_status = tool_status;
			data->inline2_operate_status = operate_status;
		}
	}

	if(nm_sync_cluster_status.rebooting) {
		data->connect = NM_CLUSTER_STATE_SYNC_REBOOT;
	} else {
		data->connect = nm_sync_cluster_config.enable ?
			NM_CLUSTER_STATE_CONNECTED :
			NM_CLUSTER_STATE_SYNC_DISABLED;
	}

	if(nm_sw_get_board_info(&sys_info) == 0) {
		snprintf(data->hostname, sizeof(data->hostname), "%s", sys_info.name);
		snprintf(data->serial, sizeof(data->serial), "%s", sys_info.serial);
		memcpy(data->mac, sys_info.mac, sizeof(data->mac));
	}

	data->hostname[sizeof(data->hostname) - 1] = '\0';
	data->serial[sizeof(data->serial) - 1] = '\0';

	return 0;
}

void nm_sync_cluster_rx(int sock, struct sockaddr_in *from,
		struct nm_sync_cluster_peer_status *data)
{
	int id;
	int old_connect;
	int clear = 0;
	struct nm_sync_cluster_peer_status *peer;

	if(!from || !data)
		return;

	if(nm_sync_cluster_config.enable == 0)
		return;

	peer = nm_sync_cluster_peer_status_get_by_ip(from->sin_addr.s_addr);
	if(!peer)
		return;

	old_connect = peer->connect;

	peer->connect = data->connect;
	peer->inline1_status = data->inline1_status;
	peer->inline2_status = data->inline2_status;
	peer->inline1_operate_status = data->inline1_operate_status;
	peer->inline2_operate_status = data->inline2_operate_status;
	peer->last_rx_ms = 0;

	snprintf(peer->hostname, sizeof(peer->hostname), "%s", data->hostname);
	snprintf(peer->serial, sizeof(peer->serial), "%s", data->serial);
	memcpy(peer->mac, data->mac, sizeof(peer->mac));

	if(data->connect == NM_CLUSTER_STATE_SYNC_REBOOT) {
		nm_sync_cluster_save_local();

		for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++)
			nm_bypass_cluster_set(id, NM_BYPASS_STATUS_BYPASS, clear);

		return;
	}

	if(data->connect == NM_CLUSTER_STATE_SYNC_DISABLED) {
		nm_sync_cluster_recover_local();
		return;
	}

	if(data->connect == NM_CLUSTER_STATE_CONNECTED &&
			old_connect == NM_CLUSTER_STATE_TIMEOUT) {
		nm_sync_cluster_recover_local();
		nm_socket_cluster_send_packet(sock);
	}

	if(data->connect == NM_CLUSTER_STATE_CONNECTED &&
			old_connect == NM_CLUSTER_STATE_SYNC_REBOOT) {
		nm_sync_cluster_recover_local();
		nm_socket_cluster_send_packet(sock);
	}

	if(nm_cluster_state.fail_action_active)
		return;

	nm_sync_cluster_save_local();

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
		nm_bypass_cluster_set(id, (id == 0) ?
				peer->inline1_status : peer->inline2_status,
				clear);
	}
}

void nm_sync_cluster_periodic(int sock, int elapsed_ms)
{
	int i;
	int id;
	int mode;
	int clear = 0;
	int tool_status;
	struct nm_sync_cluster_peer_status *peer;

	if(nm_sync_cluster_config.enable == 0)
		return;

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
		tool_status = nm_bypass_cluster_status_get(id);

		if(!nm_cluster_state.fail_action_active &&
				nm_cluster_state.local_tool[id] != tool_status)
			nm_socket_cluster_send_packet(sock);

		nm_cluster_state.local_tool[id] = tool_status;
	}

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer = &nm_sync_cluster_status.peer_status.peer[i];

		if(!peer->valid)
			continue;

		if(peer->connect != NM_CLUSTER_STATE_CONNECTED)
			continue;

		peer->last_rx_ms += elapsed_ms;

		if(peer->last_rx_ms >= (uint32_t)nm_sync_cluster_config.time_out) {
			peer->connect = NM_CLUSTER_STATE_TIMEOUT;
			mode = nm_sync_cluster_config.fail_action;

			if(mode == NM_SYNC_CLUSTER_FAIL_ACTION_ASIS) {
				nm_sync_cluster_recover_local();
				nm_sync_cluster_status.fail_action_active = 1;
				continue;
			}

			nm_cluster_state.fail_action_active = 1;
			nm_sync_cluster_status.fail_action_active = 1;

			for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++)
				nm_bypass_cluster_set(id, mode, clear);
		}
	}
}
