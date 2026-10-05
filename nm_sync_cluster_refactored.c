#include "cpss-nemon.h"

#include <errno.h>
#include <fcntl.h>
#include <stdint.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>

#ifndef NM_SOCKET_CLUSTER_RECV_BURST_MAX
#define NM_SOCKET_CLUSTER_RECV_BURST_MAX 32
#endif

#ifndef NM_SYNC_CLUSTER_MIN_TIMEOUT_FACTOR
#define NM_SYNC_CLUSTER_MIN_TIMEOUT_FACTOR 2
#endif

#ifndef NM_SYNC_CLUSTER_LOCK
#define NM_SYNC_CLUSTER_LOCK() do { } while(0)
#endif

#ifndef NM_SYNC_CLUSTER_UNLOCK
#define NM_SYNC_CLUSTER_UNLOCK() do { } while(0)
#endif

struct nm_sync_cluster_configs nm_sync_cluster_config;
struct nm_sync_cluster_peer_configs nm_sync_cluster_peer_config;

static int nm_socket_set_nonblock(int sock)
{
	int flags;

	flags = fcntl(sock, F_GETFL, 0);
	if(flags < 0)
		return -1;

	if(fcntl(sock, F_SETFL, flags | O_NONBLOCK) < 0)
		return -1;

	return 0;
}

static int nm_sync_cluster_state_is_valid(int state)
{
	switch(state) {
	case NM_SYNC_CLUSTER_STATE_SYNC_NEVER:
	case NM_SYNC_CLUSTER_STATE_CONNECTED:
	case NM_SYNC_CLUSTER_STATE_TIMEOUT:
	case NM_SYNC_CLUSTER_STATE_SYNC_REBOOT:
	case NM_SYNC_CLUSTER_STATE_SYNC_DISABLED:
		return 1;
	default:
		return 0;
	}
}

static int nm_sync_cluster_state_can_timeout(int state)
{
	return state == NM_SYNC_CLUSTER_STATE_CONNECTED ||
		state == NM_SYNC_CLUSTER_STATE_SYNC_REBOOT;
}

static int nm_sync_cluster_config_validate(
		const struct nm_sync_cluster_configs *config)
{
	if(!config)
		return -1;

	if(config->enable == 0)
		return 0;

	if(config->interval_ms < 100 || config->interval_ms > 10000)
		return -1;

	if(config->time_out < 500 || config->time_out > 300000)
		return -1;

	if(config->time_out <
			config->interval_ms * NM_SYNC_CLUSTER_MIN_TIMEOUT_FACTOR)
		return -1;

	return 0;
}

static int nm_sync_cluster_peer_config_validate(
		const struct nm_sync_cluster_peer_configs *config)
{
	int i;
	int j;
	uint32_t ip;

	if(!config)
		return -1;

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		ip = config->peer_info[i].ip;
		if(ip == 0)
			continue;

		for(j = i + 1; j < NM_SYNC_CLUSTER_PEER_MAX; j++) {
			if(ip == config->peer_info[j].ip)
				return -1;
		}
	}

	return 0;
}

static void nm_sync_cluster_touch_peer(
		struct nm_sync_cluster_peer_info *peer,
		const struct nm_sync_cluster_peer_info *data)
{
	peer->connect = data->connect;
	peer->inline1_status = data->inline1_status;
	peer->inline2_status = data->inline2_status;
	peer->inline1_operate_status = data->inline1_operate_status;
	peer->inline2_operate_status = data->inline2_operate_status;
	peer->last_rx_ms = 0;

	snprintf(peer->hostname, sizeof(peer->hostname), "%s", data->hostname);
	snprintf(peer->serial, sizeof(peer->serial), "%s", data->serial);
	memcpy(peer->mac, data->mac, sizeof(peer->mac));
}

void nm_sync_cluster_save_local(void)
{
	int id;
	int tool_status;

	if(nm_cluster_state.saved)
		return;

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
		tool_status = nm_bypass_cluster_status_get(id);
		nm_cluster_state.saved_tool[id] = tool_status;
		nm_cluster_state.local_tool[id] = tool_status;
	}

	nm_cluster_state.saved = 1;
	nm_cluster_state.boot_sync_time = 1;
}

void nm_sync_cluster_recover_local(void)
{
	int id;
	int had_saved;

	had_saved = nm_cluster_state.saved;

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
		if(had_saved) {
			nm_bypass_cluster_set(id, nm_cluster_state.saved_tool[id]);
			nm_cluster_state.local_tool[id] =
				nm_cluster_state.saved_tool[id];
		}

		nm_bypass_cluster_clear(id);
	}

	nm_cluster_state.saved = 0;
	nm_cluster_state.fail_action_active = 0;
	nm_cluster_state.fail_action_mode = -1;
	nm_sync_cluster_config.fail_action_active = 0;
}

void nm_sync_cluster_init(void)
{
	int i;

	NM_SYNC_CLUSTER_LOCK();

	memset(&nm_cluster_state, 0, sizeof(nm_cluster_state));
	nm_cluster_state.fail_action_mode = -1;
	nm_sync_cluster_config.fail_action_active = 0;

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		if(nm_sync_cluster_peer_config.peer_info[i].ip == 0)
			continue;

		nm_sync_cluster_peer_config.peer_info[i].connect =
			NM_SYNC_CLUSTER_STATE_SYNC_NEVER;
		nm_sync_cluster_peer_config.peer_info[i].last_rx_ms = 0;
	}

	NM_SYNC_CLUSTER_UNLOCK();
}

int nm_sync_cluster_peer_ip_get(int index, uint32_t *ip)
{
	if(!ip || index < 0 || index >= NM_SYNC_CLUSTER_PEER_MAX)
		return -1;

	NM_SYNC_CLUSTER_LOCK();
	*ip = nm_sync_cluster_peer_config.peer_info[index].ip;
	NM_SYNC_CLUSTER_UNLOCK();

	return 0;
}

int nm_sync_cluster_make_tx_data(struct nm_sync_cluster_peer_info *data)
{
	int id;
	int status = 0;
	int active = 0;
	int tool_status;
	int tool_id;
	int operate_status;
	struct system_information sys_info;

	if(!data)
		return -1;

	memset(data, 0, sizeof(*data));
	memset(&sys_info, 0, sizeof(sys_info));

	NM_SYNC_CLUSTER_LOCK();

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
		tool_id = id + 1;

		if(nm_cluster_state.fail_action_active &&
				nm_cluster_state.saved) {
			tool_status = nm_cluster_state.saved_tool[id];
		} else {
			tool_status = nm_bypass_cluster_status_get(id);
			nm_cluster_state.local_tool[id] = tool_status;
		}

		operate_status = tool_status;

		if(nm_inl_bypass_status_get(tool_id, &status, &active) == 0) {
			operate_status =
				(status == NM_BYPASS_STATUS_INLINE) ? 1 : 0;
		}

		if(tool_id == 1) {
			data->inline1_status = tool_status;
			data->inline1_operate_status = operate_status;
		} else {
			data->inline2_status = tool_status;
			data->inline2_operate_status = operate_status;
		}
	}

	if(nm_sync_cluster_config.reboot) {
		data->connect = NM_SYNC_CLUSTER_STATE_SYNC_REBOOT;
	} else if(nm_sync_cluster_config.enable) {
		data->connect = NM_SYNC_CLUSTER_STATE_CONNECTED;
	} else {
		data->connect = NM_SYNC_CLUSTER_STATE_SYNC_DISABLED;
	}

	NM_SYNC_CLUSTER_UNLOCK();

	if(nm_sw_get_board_info(&sys_info) == 0) {
		snprintf(data->hostname, sizeof(data->hostname),
				"%s", sys_info.name);
		snprintf(data->serial, sizeof(data->serial),
				"%s", sys_info.serial);
		memcpy(data->mac, sys_info.mac, sizeof(data->mac));
	}

	data->hostname[sizeof(data->hostname) - 1] = '\0';
	data->serial[sizeof(data->serial) - 1] = '\0';

	return 0;
}

int nm_socket_cluster_send_open(void)
{
	int sock;

	sock = socket(AF_INET, SOCK_DGRAM, 0);
	if(sock < 0) {
		osPrintf("cluster send socket failed errno=%d\n", errno);
		return -1;
	}

	return sock;
}

int nm_socket_cluster_send_packet(int sock)
{
	int i;
	int rc;
	int result = 0;
	uint32_t ip;
	struct sockaddr_in peer_addr;
	struct nm_sync_cluster_peer_info data;

	if(sock < 0)
		return -1;

	if(nm_sync_cluster_make_tx_data(&data) < 0)
		return -1;

	memset(&peer_addr, 0, sizeof(peer_addr));
	peer_addr.sin_family = AF_INET;
	peer_addr.sin_port = htons(NM_SYNC_PORT);

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		if(nm_sync_cluster_peer_ip_get(i, &ip) < 0 || ip == 0)
			continue;

		peer_addr.sin_addr.s_addr = ip;

		do {
			rc = sendto(sock, (const char *)&data, sizeof(data), 0,
					(struct sockaddr *)&peer_addr,
					sizeof(peer_addr));
		} while(rc < 0 && errno == EINTR);

		if(rc != (int)sizeof(data)) {
			osPrintf("cluster send failed peer=%u errno=%d\n",
					(unsigned int)ip, errno);
			result = -1;
		}
	}

	return result;
}

static void nm_sync_cluster_send_once(void)
{
	int sock;

	sock = nm_socket_cluster_send_open();
	if(sock < 0)
		return;

	nm_socket_cluster_send_packet(sock);
	close(sock);
}

int nm_socket_cluster_peer_config_set(
		struct nm_sync_cluster_peer_configs *config)
{
	int i;

	if(nm_sync_cluster_peer_config_validate(config) < 0)
		return -1;

	NM_SYNC_CLUSTER_LOCK();

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		if(nm_sync_cluster_peer_config.peer_info[i].ip ==
				config->peer_info[i].ip)
			continue;

		memset(&nm_sync_cluster_peer_config.peer_info[i], 0,
				sizeof(nm_sync_cluster_peer_config.peer_info[i]));

		if(config->peer_info[i].ip == 0)
			continue;

		nm_sync_cluster_peer_config.peer_info[i].ip =
			config->peer_info[i].ip;
		nm_sync_cluster_peer_config.peer_info[i].connect =
			NM_SYNC_CLUSTER_STATE_SYNC_NEVER;
	}

	NM_SYNC_CLUSTER_UNLOCK();

	nm_sync_cluster_send_once();

	return 0;
}

int nm_socket_cluster_config_set(struct nm_sync_cluster_configs *config)
{
	if(nm_sync_cluster_config_validate(config) < 0)
		return -1;

	NM_SYNC_CLUSTER_LOCK();
	nm_sync_cluster_config = *config;
	nm_sync_cluster_config.fail_action_active =
		nm_cluster_state.fail_action_active;
	NM_SYNC_CLUSTER_UNLOCK();

	nm_sync_cluster_send_once();

	if(config->enable == 0) {
		NM_SYNC_CLUSTER_LOCK();
		nm_sync_cluster_recover_local();
		NM_SYNC_CLUSTER_UNLOCK();
	}

	return 0;
}

void nm_sync_cluster_rx(int sock, struct sockaddr_in *from,
		struct nm_sync_cluster_peer_info *data)
{
	int i;
	int id;
	int old_connect;
	struct nm_sync_cluster_peer_info *peer;

	if(!from || !data)
		return;

	if(!nm_sync_cluster_state_is_valid(data->connect))
		return;

	NM_SYNC_CLUSTER_LOCK();

	if(nm_sync_cluster_config.enable == 0) {
		NM_SYNC_CLUSTER_UNLOCK();
		return;
	}

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer = &nm_sync_cluster_peer_config.peer_info[i];

		if(peer->ip != from->sin_addr.s_addr)
			continue;

		old_connect = peer->connect;
		nm_sync_cluster_touch_peer(peer, data);

		if(data->connect == NM_SYNC_CLUSTER_STATE_SYNC_REBOOT) {
			nm_sync_cluster_save_local();

			for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++)
				nm_bypass_cluster_set(id, 0);

			NM_SYNC_CLUSTER_UNLOCK();
			return;
		}

		if(data->connect == NM_SYNC_CLUSTER_STATE_SYNC_DISABLED) {
			nm_sync_cluster_recover_local();
			NM_SYNC_CLUSTER_UNLOCK();
			return;
		}

		if(data->connect == NM_SYNC_CLUSTER_STATE_CONNECTED &&
				(old_connect == NM_SYNC_CLUSTER_STATE_TIMEOUT ||
				 old_connect == NM_SYNC_CLUSTER_STATE_SYNC_REBOOT)) {
			nm_sync_cluster_recover_local();
			NM_SYNC_CLUSTER_UNLOCK();
			nm_socket_cluster_send_packet(sock);
			NM_SYNC_CLUSTER_LOCK();
		}

		if(nm_cluster_state.fail_action_active) {
			NM_SYNC_CLUSTER_UNLOCK();
			return;
		}

		nm_sync_cluster_save_local();

		for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
			nm_bypass_cluster_set(id, (id == 0) ?
					peer->inline1_status :
					peer->inline2_status);
		}

		NM_SYNC_CLUSTER_UNLOCK();
		return;
	}

	NM_SYNC_CLUSTER_UNLOCK();
}

static void nm_sync_cluster_enter_fail_action(int sock, int mode)
{
	int id;

	nm_sync_cluster_save_local();

	if(mode == NM_SYNC_CLUSTER_FAIL_ACTION_ASIS) {
		nm_sync_cluster_recover_local();
		nm_cluster_state.fail_action_active = 1;
		nm_sync_cluster_config.fail_action_active = 1;
		nm_cluster_state.fail_action_mode = mode;
		return;
	}

	nm_cluster_state.fail_action_active = 1;
	nm_sync_cluster_config.fail_action_active = 1;
	nm_cluster_state.fail_action_mode = mode;

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++)
		nm_bypass_cluster_fail_action_set(id, mode);

	NM_SYNC_CLUSTER_UNLOCK();
	nm_socket_cluster_send_packet(sock);
	NM_SYNC_CLUSTER_LOCK();
}

void nm_sync_cluster_periodic(int sock, int elapsed_ms)
{
	int i;
	int id;
	int mode;
	int local_changed = 0;
	int timed_out_state;
	int tool_status;
	struct nm_sync_cluster_peer_info *peer;

	if(elapsed_ms <= 0)
		return;

	NM_SYNC_CLUSTER_LOCK();

	if(nm_sync_cluster_config.enable == 0) {
		NM_SYNC_CLUSTER_UNLOCK();
		return;
	}

	for(id = 0; id < NM_SYNC_CLUSTER_INLINE_MAX; id++) {
		tool_status = nm_bypass_cluster_status_get(id);

		if(!nm_cluster_state.fail_action_active &&
				nm_cluster_state.local_tool[id] != tool_status) {
			local_changed = 1;
		}

		nm_cluster_state.local_tool[id] = tool_status;
	}

	if(local_changed) {
		NM_SYNC_CLUSTER_UNLOCK();
		nm_socket_cluster_send_packet(sock);
		NM_SYNC_CLUSTER_LOCK();
	}

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer = &nm_sync_cluster_peer_config.peer_info[i];

		if(peer->ip == 0)
			continue;

		if(!nm_sync_cluster_state_can_timeout(peer->connect))
			continue;

		if(peer->last_rx_ms < (uint32_t)nm_sync_cluster_config.time_out)
			peer->last_rx_ms += elapsed_ms;

		if(peer->last_rx_ms < (uint32_t)nm_sync_cluster_config.time_out)
			continue;

		timed_out_state = peer->connect;
		peer->connect = NM_SYNC_CLUSTER_STATE_TIMEOUT;

		if(timed_out_state == NM_SYNC_CLUSTER_STATE_SYNC_REBOOT) {
			nm_sync_cluster_recover_local();
			NM_SYNC_CLUSTER_UNLOCK();
			nm_socket_cluster_send_packet(sock);
			NM_SYNC_CLUSTER_LOCK();
			continue;
		}

		if(nm_cluster_state.fail_action_active)
			continue;

		mode = nm_sync_cluster_config.fail_action;
		nm_sync_cluster_enter_fail_action(sock, mode);

		if(mode == NM_SYNC_CLUSTER_FAIL_ACTION_ASIS) {
			NM_SYNC_CLUSTER_UNLOCK();
			nm_socket_cluster_send_packet(sock);
			NM_SYNC_CLUSTER_LOCK();
		}
	}

	NM_SYNC_CLUSTER_UNLOCK();
}

int nm_socket_cluster_recv_open(void)
{
	int sock;
	int opt = 1;
	struct sockaddr_in local_addr;

	sock = socket(AF_INET, SOCK_DGRAM, 0);
	if(sock < 0) {
		osPrintf("cluster recv socket failed errno=%d\n", errno);
		return -1;
	}

	if(setsockopt(sock, SOL_SOCKET, SO_REUSEADDR,
			(const char *)&opt, sizeof(opt)) < 0) {
		osPrintf("cluster setsockopt reuseaddr failed errno=%d\n", errno);
		close(sock);
		return -1;
	}

#ifdef SO_REUSEPORT
	setsockopt(sock, SOL_SOCKET, SO_REUSEPORT,
			(const char *)&opt, sizeof(opt));
#endif

	if(nm_socket_set_nonblock(sock) < 0) {
		osPrintf("cluster non-blocking setup failed errno=%d\n", errno);
		close(sock);
		return -1;
	}

	memset(&local_addr, 0, sizeof(local_addr));
	local_addr.sin_family = AF_INET;
	local_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	local_addr.sin_port = htons(NM_SYNC_PORT);

	if(bind(sock, (struct sockaddr *)&local_addr, sizeof(local_addr)) < 0) {
		osPrintf("cluster bind failed port=%d errno=%d\n",
				NM_SYNC_PORT, errno);
		close(sock);
		return -1;
	}

	return sock;
}

int nm_socket_cluster_recv_packet(int sock)
{
	int rc;
	int count;
	struct sockaddr_in from_addr;
	socklen_t from_len;
	struct nm_sync_cluster_peer_info data;

	if(sock < 0)
		return -1;

	for(count = 0; count < NM_SOCKET_CLUSTER_RECV_BURST_MAX; count++) {
		memset(&data, 0, sizeof(data));
		memset(&from_addr, 0, sizeof(from_addr));
		from_len = sizeof(from_addr);

		rc = recvfrom(sock, (char *)&data, sizeof(data), 0,
				(struct sockaddr *)&from_addr, &from_len);
		if(rc < 0) {
			if(errno == EINTR)
				continue;

			if(errno == EAGAIN || errno == EWOULDBLOCK)
				return 0;

			osPrintf("cluster recv failed errno=%d\n", errno);
			return -1;
		}

		if(rc != (int)sizeof(data))
			continue;

		data.hostname[sizeof(data.hostname) - 1] = '\0';
		data.serial[sizeof(data.serial) - 1] = '\0';

		nm_sync_cluster_rx(sock, &from_addr, &data);
	}

	return 0;
}

unsigned __TASKCONV nm_socket_cluster_recv(GT_VOID *unused)
{
	int sock = -1;
	int rc;

	(void)unused;

	nm_sync_cluster_init();

	while(1) {
		if(nm_sync_cluster_config.enable == 0) {
			if(sock >= 0) {
				close(sock);
				sock = -1;
			}

			osTimerWkAfter(LLCF_PERIOD);
			continue;
		}

		if(sock < 0) {
			sock = nm_socket_cluster_recv_open();
			if(sock < 0) {
				osTimerWkAfter(NM_SOCKET_CLUSTER_RETRY_MS);
				continue;
			}
		}

		rc = nm_socket_cluster_recv_packet(sock);
		if(rc < 0) {
			close(sock);
			sock = -1;
			osTimerWkAfter(NM_SOCKET_CLUSTER_RETRY_MS);
			continue;
		}

		nm_sync_cluster_periodic(sock, LLCF_PERIOD);
		osTimerWkAfter(LLCF_PERIOD);
	}

	return 0;
}

unsigned __TASKCONV nm_socket_cluster_send(GT_VOID *unused)
{
	int sock = -1;
	int elapsed_ms = 0;
	int interval_ms;

	(void)unused;

	while(1) {
		if(nm_sync_cluster_config.enable == 0) {
			if(sock >= 0) {
				close(sock);
				sock = -1;
			}

			elapsed_ms = 0;
			osTimerWkAfter(LLCF_PERIOD);
			continue;
		}

		if(sock < 0) {
			sock = nm_socket_cluster_send_open();
			if(sock < 0) {
				osTimerWkAfter(LLCF_PERIOD);
				continue;
			}

			elapsed_ms = nm_sync_cluster_config.interval_ms;
		}

		interval_ms = nm_sync_cluster_config.interval_ms;
		if(interval_ms <= 0)
			interval_ms = LLCF_PERIOD;

		if(elapsed_ms >= interval_ms) {
			if(nm_socket_cluster_send_packet(sock) < 0) {
				close(sock);
				sock = -1;
			}

			elapsed_ms = 0;
		}

		osTimerWkAfter(LLCF_PERIOD);
		elapsed_ms += LLCF_PERIOD;
	}

	return 0;
}
