/*
 * Drop-in replacements for the Zebra cluster functions after moving peer
 * config into struct nm_sync_cluster_config.
 *
 * Assumed globals in zebra:
 *   struct nm_sync_cluster_config nm_sync_cluster_configs;
 *   struct nm_sync_cluster_status nm_sync_cluster_state;
 */

#include <errno.h>
#include <fcntl.h>
#include <unistd.h>

#ifndef NM_SYNC_CLUSTER_KEY_LEN
#define NM_SYNC_CLUSTER_KEY_LEN		16
#endif

#ifndef NM_SYNC_CLUSTER_KEY_HEX_LEN
#define NM_SYNC_CLUSTER_KEY_HEX_LEN	(NM_SYNC_CLUSTER_KEY_LEN * 2)
#endif

static int nm_cluster_key_generate(
		uint8_t key[NM_SYNC_CLUSTER_KEY_LEN])
{
	int fd;
	ssize_t len;
	size_t offset = 0;

	if(!key)
		return -1;

	fd = open("/dev/urandom", O_RDONLY);
	if(fd < 0)
		return -1;

	while(offset < NM_SYNC_CLUSTER_KEY_LEN) {
		len = read(fd, key + offset,
				NM_SYNC_CLUSTER_KEY_LEN - offset);

		if(len < 0 && errno == EINTR)
			continue;

		if(len <= 0) {
			close(fd);
			memset(key, 0, NM_SYNC_CLUSTER_KEY_LEN);
			return -1;
		}

		offset += (size_t)len;
	}

	close(fd);
	return 0;
}

static void nm_cluster_key_to_hex(
		const uint8_t key[NM_SYNC_CLUSTER_KEY_LEN],
		char hex[NM_SYNC_CLUSTER_KEY_HEX_LEN + 1])
{
	int i;
	static const char digits[] = "0123456789abcdef";

	for(i = 0; i < NM_SYNC_CLUSTER_KEY_LEN; i++) {
		hex[i * 2] = digits[(key[i] >> 4) & 0x0f];
		hex[i * 2 + 1] = digits[key[i] & 0x0f];
	}

	hex[NM_SYNC_CLUSTER_KEY_HEX_LEN] = '\0';
}

static int nm_cluster_key_from_hex(
		const char *hex,
		uint8_t key[NM_SYNC_CLUSTER_KEY_LEN])
{
	int i;
	int hi;
	int lo;
	char c;
	uint8_t parsed[NM_SYNC_CLUSTER_KEY_LEN];

	if(!hex || !key || strlen(hex) != NM_SYNC_CLUSTER_KEY_HEX_LEN)
		return -1;

	for(i = 0; i < NM_SYNC_CLUSTER_KEY_LEN; i++) {
		c = hex[i * 2];
		if(c >= '0' && c <= '9')
			hi = c - '0';
		else if(c >= 'a' && c <= 'f')
			hi = c - 'a' + 10;
		else if(c >= 'A' && c <= 'F')
			hi = c - 'A' + 10;
		else
			return -1;

		c = hex[i * 2 + 1];
		if(c >= '0' && c <= '9')
			lo = c - '0';
		else if(c >= 'a' && c <= 'f')
			lo = c - 'a' + 10;
		else if(c >= 'A' && c <= 'F')
			lo = c - 'A' + 10;
		else
			return -1;

		parsed[i] = (uint8_t)((hi << 4) | lo);
	}

	memcpy(key, parsed, sizeof(parsed));
	return 0;
}

static struct nm_sync_cluster_peer_status *
nm_cluster_peer_status_find(struct in_addr ip)
{
	int i;
	struct nm_sync_cluster_peer_status *status;

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		status = &nm_sync_cluster_state.peer_status.peer[i];

		if(!status->valid)
			continue;

		if(status->ip.s_addr == ip.s_addr)
			return status;
	}

	return NULL;
}

void nm_sys_init_cluster_config(void)
{
	memset(&nm_sync_cluster_configs, 0, sizeof(nm_sync_cluster_configs));
	memset(&nm_sync_cluster_state, 0, sizeof(nm_sync_cluster_state));

	nm_sync_cluster_configs.enable = 0;
	nm_sync_cluster_configs.mode = NM_SYNC_CLUSTER_MODE_DUAL;
	nm_sync_cluster_configs.interval_ms = 5000;
	nm_sync_cluster_configs.time_out = 30000;
	nm_sync_cluster_configs.fail_action = NM_SYNC_CLUSTER_FAIL_ACTION_ASIS;
	nm_sync_cluster_configs.peer.count = 0;

	nm_napi_set_cluster_config(&nm_sync_cluster_configs);
}

DEFUN(nm_cluster_sync_enable,
		nm_cluster_sync_enable_cmd,
		"sync (enable|disable)",
		"Configure cluster sync\n"
		"Enable cluster sync\n"
		"Disable cluster sync\n")
{
	int rc;
	int key_generated = 0;
	char hex[NM_SYNC_CLUSTER_KEY_HEX_LEN + 1];
	static const uint8_t zero_key[NM_SYNC_CLUSTER_KEY_LEN] = {0};

	if(!strcmp(argv[0], "disable")) {
		nm_sync_cluster_configs.enable = 0;
	} else if(memcmp(nm_sync_cluster_configs.key, zero_key,
				sizeof(nm_sync_cluster_configs.key)) == 0) {
		if(nm_cluster_key_generate(
					nm_sync_cluster_configs.key) < 0) {
			vty_out(vty,
					"%% Failed to generate cluster DTLS key%s",
					VTY_NEWLINE);
			return CMD_WARNING;
		}

		key_generated = 1;
		nm_sync_cluster_configs.enable = 0;
	} else {
		nm_sync_cluster_configs.enable = 1;
	}

	rc = nm_napi_set_cluster_config(&nm_sync_cluster_configs);
	if(rc < 0) {
		if(key_generated)
			memset(nm_sync_cluster_configs.key, 0,
					NM_SYNC_CLUSTER_KEY_LEN);

		vty_out(vty, "%% Failed to update cluster config to appDemo%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	if(!key_generated)
		return CMD_SUCCESS;

	nm_cluster_key_to_hex(nm_sync_cluster_configs.key, hex);

	vty_out(vty, "%% Cluster DTLS key was not set.%s", VTY_NEWLINE);
	vty_out(vty, "%% Generated key: %s%s", hex, VTY_NEWLINE);
	vty_out(vty, "%% Configure the same key on the peer device:%s",
			VTY_NEWLINE);
	vty_out(vty, "%%   cluster%s", VTY_NEWLINE);
	vty_out(vty, "%%   key %s%s", hex, VTY_NEWLINE);
	vty_out(vty, "%% Then run 'sync enable' on both devices.%s",
			VTY_NEWLINE);

	return CMD_WARNING;
}

DEFUN(nm_cluster_key,
		nm_cluster_key_cmd,
		"key WORD",
		"Configure cluster DTLS key\n"
		"16-byte key as 32 hexadecimal characters\n")
{
	int rc;
	uint8_t key[NM_SYNC_CLUSTER_KEY_LEN];

	if(nm_cluster_key_from_hex(argv[0], key) < 0) {
		vty_out(vty, "%% Invalid key. Use exactly 32 hexadecimal characters.%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	memcpy(nm_sync_cluster_configs.key, key, sizeof(key));

	rc = nm_napi_set_cluster_config(&nm_sync_cluster_configs);
	if(rc < 0) {
		vty_out(vty, "%% Failed to update cluster config to appDemo%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	vty_out(vty, "Cluster DTLS key configured.%s", VTY_NEWLINE);
	return CMD_SUCCESS;
}

DEFUN(nm_cluster_mode,
		nm_cluster_mode_cmd,
		"mode (dual|mesh)",
		"Configure cluster mode\n"
		"Dual cluster mode\n"
		"Mesh cluster mode\n")
{
	int rc;

	nm_sync_cluster_configs.mode =
		(!strcmp(argv[0], "dual")) ?
		NM_SYNC_CLUSTER_MODE_DUAL : NM_SYNC_CLUSTER_MODE_MESH;

	rc = nm_napi_set_cluster_config(&nm_sync_cluster_configs);
	if(rc < 0) {
		vty_out(vty, "%% Failed to update cluster config to appDemo%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	return CMD_SUCCESS;
}

DEFUN(nm_cluster_interval,
		nm_cluster_interval_cmd,
		"interval <100-10000>",
		"Configure cluster sync interval\n"
		"Interval milliseconds\n")
{
	int rc;

	nm_sync_cluster_configs.interval_ms = atoi(argv[0]);

	rc = nm_napi_set_cluster_config(&nm_sync_cluster_configs);
	if(rc < 0) {
		vty_out(vty, "%% Failed to update cluster config to appDemo%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	return CMD_SUCCESS;
}

DEFUN(nm_cluster_timeout,
		nm_cluster_timeout_cmd,
		"time-out <500-3000000>",
		"Configure cluster sync timeout\n"
		"Timeout milliseconds\n")
{
	int rc;

	nm_sync_cluster_configs.time_out = atoi(argv[0]);

	rc = nm_napi_set_cluster_config(&nm_sync_cluster_configs);
	if(rc < 0) {
		vty_out(vty, "%% Failed to update cluster config to appDemo%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	return CMD_SUCCESS;
}

DEFUN(nm_cluster_fail_action,
		nm_cluster_fail_action_cmd,
		"fail-action (bypass|inline|as-is)",
		"Configure fail action\n"
		"Change to bypass mode\n"
		"Change to inline mode\n"
		"Keep current mode\n")
{
	int rc;

	if(!strcmp(argv[0], "bypass"))
		nm_sync_cluster_configs.fail_action =
			NM_SYNC_CLUSTER_FAIL_ACTION_BYPASS;
	else if(!strcmp(argv[0], "inline"))
		nm_sync_cluster_configs.fail_action =
			NM_SYNC_CLUSTER_FAIL_ACTION_INLINE;
	else
		nm_sync_cluster_configs.fail_action =
			NM_SYNC_CLUSTER_FAIL_ACTION_ASIS;

	rc = nm_napi_set_cluster_config(&nm_sync_cluster_configs);
	if(rc < 0) {
		vty_out(vty, "%% Failed to update cluster config to appDemo%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	return CMD_SUCCESS;
}

DEFUN(nm_cluster_peer_ip_add,
		nm_cluster_peer_ip_add_cmd,
		"peer-ip add A.B.C.D",
		"Configure peer IP\n"
		"Add peer IP\n"
		"Peer IPv4 address\n")
{
	int i;
	int rc;
	int empty = -1;
	struct prefix_ipv4 cp;
	struct nm_sync_cluster_peer_info *peer;

	if(str2prefix_ipv4(argv[0], &cp) <= 0) {
		vty_out(vty, "%% Malformed address%s", VTY_NEWLINE);
		return CMD_WARNING;
	}

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer = &nm_sync_cluster_configs.peer.list[i];

		if(peer->valid && peer->ip.s_addr == cp.prefix.s_addr) {
			vty_out(vty, "%% peer-ip already exists%s", VTY_NEWLINE);
			return CMD_WARNING;
		}

		if(empty < 0 && !peer->valid)
			empty = i;
	}

	if(empty < 0) {
		vty_out(vty, "%% peer-ip table full%s", VTY_NEWLINE);
		return CMD_WARNING;
	}

	memset(&nm_sync_cluster_configs.peer.list[empty], 0,
			sizeof(struct nm_sync_cluster_peer_info));
	nm_sync_cluster_configs.peer.list[empty].valid = 1;
	nm_sync_cluster_configs.peer.list[empty].ip = cp.prefix;
	nm_sync_cluster_configs.peer.count++;

	rc = nm_napi_set_cluster_config(&nm_sync_cluster_configs);
	if(rc < 0) {
		vty_out(vty,
				"%% Failed to update cluster peer config to appDemo%s",
				VTY_NEWLINE);
		return CMD_WARNING;
	}

	return CMD_SUCCESS;
}

DEFUN(nm_cluster_peer_ip_remove,
		nm_cluster_peer_ip_remove_cmd,
		"peer-ip remove A.B.C.D",
		"Configure peer IP\n"
		"Remove peer IP\n"
		"Peer IPv4 address\n")
{
	int i;
	int rc;
	struct prefix_ipv4 cp;
	struct nm_sync_cluster_peer_info *peer;

	if(str2prefix_ipv4(argv[0], &cp) <= 0) {
		vty_out(vty, "%% Malformed address%s", VTY_NEWLINE);
		return CMD_WARNING;
	}

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer = &nm_sync_cluster_configs.peer.list[i];

		if(peer->valid && peer->ip.s_addr == cp.prefix.s_addr) {
			memset(peer, 0, sizeof(*peer));

			if(nm_sync_cluster_configs.peer.count > 0)
				nm_sync_cluster_configs.peer.count--;

			rc = nm_napi_set_cluster_config(
					&nm_sync_cluster_configs);
			if(rc < 0) {
				vty_out(vty,
						"%% Failed to update cluster peer config to appDemo%s",
						VTY_NEWLINE);
				return CMD_WARNING;
			}

			return CMD_SUCCESS;
		}
	}

	vty_out(vty, "%% peer-ip not found%s", VTY_NEWLINE);

	return CMD_WARNING;
}

DEFUN(show_nm_cluster,
		show_nm_cluster_cmd,
		"show cluster",
		SHOW_STR
		"Cluster sync information\n")
{
	int i;
	int count = 0;
	int mac_empty;
	char ip_str[INET_ADDRSTRLEN];
	char mac_str[18];
	const char *connect_str;
	const char *inline1_str;
	const char *inline2_str;
	const char *operate1_str;
	const char *operate2_str;
	const char *hostname_str;
	const char *serial_str;
	const char *mac_out_str;
	struct nm_sync_cluster_peer_info *peer_conf;
	struct nm_sync_cluster_peer_status *peer_status;

	vty_out(vty, "Cluster Configuration%s", VTY_NEWLINE);
	vty_out(vty, "--------------------------------------------------%s", VTY_NEWLINE);
	vty_out(vty, "  Sync        : %s%s",
			nm_sync_cluster_configs.enable ? "enable" : "disable",
			VTY_NEWLINE);
	vty_out(vty, "  Mode        : %s%s",
			(nm_sync_cluster_configs.mode == NM_SYNC_CLUSTER_MODE_DUAL) ?
			"dual" : "mesh", VTY_NEWLINE);
	vty_out(vty, "  Interval    : %d ms%s",
			nm_sync_cluster_configs.interval_ms, VTY_NEWLINE);
	vty_out(vty, "  Time-out    : %d ms%s",
			nm_sync_cluster_configs.time_out, VTY_NEWLINE);
	vty_out(vty, "  Fail-action : %s%s",
			(nm_sync_cluster_configs.fail_action ==
			 NM_SYNC_CLUSTER_FAIL_ACTION_BYPASS) ? "bypass" :
			(nm_sync_cluster_configs.fail_action ==
			 NM_SYNC_CLUSTER_FAIL_ACTION_INLINE) ? "inline" : "as-is",
			VTY_NEWLINE);
	vty_out(vty, "  Fail-action Active : %s%s",
			nm_sync_cluster_state.fail_action_active ?
			"active" : "inactive", VTY_NEWLINE);

	vty_out(vty, "%s", VTY_NEWLINE);
	vty_out(vty, "Cluster Peer List%s", VTY_NEWLINE);
	vty_out(vty,
			"------------------------------------------------------------------------------------------------------------------------%s",
			VTY_NEWLINE);
	vty_out(vty,
			" %-3s %-15s %-8s %-8s %-8s %-8s %-8s %-18s %-18s %-17s%s",
			"No.", "IP Address", "Connect", "Tool1", "Tool2",
			"Sync_Tool1", "Sync_Tool2", "Hostname", "Serial",
			"MAC", VTY_NEWLINE);
	vty_out(vty,
			"------------------------------------------------------------------------------------------------------------------------%s",
			VTY_NEWLINE);

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer_conf = &nm_sync_cluster_configs.peer.list[i];

		if(!peer_conf->valid)
			continue;

		memset(ip_str, 0, sizeof(ip_str));
		memset(mac_str, 0, sizeof(mac_str));

		connect_str = "NEVER";
		inline1_str = "-";
		inline2_str = "-";
		operate1_str = "-";
		operate2_str = "-";
		hostname_str = "-";
		serial_str = "-";
		mac_out_str = "-";

		if(!inet_ntop(AF_INET, &peer_conf->ip, ip_str, sizeof(ip_str)))
			snprintf(ip_str, sizeof(ip_str), "-");

		peer_status = nm_cluster_peer_status_find(peer_conf->ip);
		if(peer_status) {
			switch(peer_status->connect) {
			case NM_CLUSTER_STATE_CONNECTED:
				connect_str = "OK";
				break;
			case NM_CLUSTER_STATE_TIMEOUT:
				connect_str = "LOST";
				break;
			case NM_CLUSTER_STATE_SYNC_DISABLED:
				connect_str = "SYNC-OFF";
				break;
			case NM_CLUSTER_STATE_SYNC_NEVER:
				connect_str = "NEVER";
				break;
			case NM_CLUSTER_STATE_SYNC_REBOOT:
				connect_str = "REBOOT";
				break;
			default:
				connect_str = "UNKNOWN";
				break;
			}

			if(peer_status->connect == NM_CLUSTER_STATE_CONNECTED) {
				inline1_str = peer_status->inline1_status ?
					"INLINE" : "BYPASS";
				inline2_str = peer_status->inline2_status ?
					"INLINE" : "BYPASS";
				operate1_str = peer_status->inline1_operate_status ?
					"INLINE" : "BYPASS";
				operate2_str = peer_status->inline2_operate_status ?
					"INLINE" : "BYPASS";

				if(peer_status->hostname[0])
					hostname_str = peer_status->hostname;
				if(peer_status->serial[0])
					serial_str = peer_status->serial;

				mac_empty =
					(peer_status->mac[0] == 0) &&
					(peer_status->mac[1] == 0) &&
					(peer_status->mac[2] == 0) &&
					(peer_status->mac[3] == 0) &&
					(peer_status->mac[4] == 0) &&
					(peer_status->mac[5] == 0);

				if(!mac_empty) {
					snprintf(mac_str, sizeof(mac_str),
							"%02x:%02x:%02x:%02x:%02x:%02x",
							peer_status->mac[0] & 0xff,
							peer_status->mac[1] & 0xff,
							peer_status->mac[2] & 0xff,
							peer_status->mac[3] & 0xff,
							peer_status->mac[4] & 0xff,
							peer_status->mac[5] & 0xff);
					mac_out_str = mac_str;
				}
			}
		}

		vty_out(vty,
				" %-3d %-15s %-8s %-8s %-8s %-8s %-8s %-18.18s %-18.18s %-17s%s",
				i + 1, ip_str, connect_str, inline1_str,
				inline2_str, operate1_str, operate2_str,
				hostname_str, serial_str, mac_out_str, VTY_NEWLINE);
		count++;
	}

	if(count == 0)
		vty_out(vty, " Not set%s", VTY_NEWLINE);

	vty_out(vty,
			"------------------------------------------------------------------------------------------------------------------------%s",
			VTY_NEWLINE);
	vty_out(vty, "%s", VTY_NEWLINE);

	return CMD_SUCCESS;
}

void nm_cluster_config_write(struct vty *vty)
{
	int i;
	int key_set = 0;
	char ip_str[INET_ADDRSTRLEN];
	char key_hex[NM_SYNC_CLUSTER_KEY_HEX_LEN + 1];
	struct nm_sync_cluster_peer_info *peer;

	vty_out(vty, "cluster%s", VTY_NEWLINE);

	for(i = 0; i < NM_SYNC_CLUSTER_KEY_LEN; i++) {
		if(nm_sync_cluster_configs.key[i] != 0) {
			key_set = 1;
			break;
		}
	}

	if(key_set) {
		nm_cluster_key_to_hex(nm_sync_cluster_configs.key, key_hex);
		vty_out(vty, " key %s%s", key_hex, VTY_NEWLINE);
	}

	vty_out(vty, " sync %s%s",
			nm_sync_cluster_configs.enable ? "enable" : "disable",
			VTY_NEWLINE);
	vty_out(vty, " mode %s%s",
			(nm_sync_cluster_configs.mode == NM_SYNC_CLUSTER_MODE_DUAL) ?
			"dual" : "mesh", VTY_NEWLINE);
	vty_out(vty, " interval %d%s",
			nm_sync_cluster_configs.interval_ms, VTY_NEWLINE);
	vty_out(vty, " time-out %d%s",
			nm_sync_cluster_configs.time_out, VTY_NEWLINE);
	vty_out(vty, " fail-action %s%s",
			(nm_sync_cluster_configs.fail_action ==
			 NM_SYNC_CLUSTER_FAIL_ACTION_BYPASS) ? "bypass" :
			(nm_sync_cluster_configs.fail_action ==
			 NM_SYNC_CLUSTER_FAIL_ACTION_INLINE) ? "inline" : "as-is",
			VTY_NEWLINE);

	for(i = 0; i < NM_SYNC_CLUSTER_PEER_MAX; i++) {
		peer = &nm_sync_cluster_configs.peer.list[i];

		if(!peer->valid)
			continue;

		if(!inet_ntop(AF_INET, &peer->ip, ip_str, sizeof(ip_str)))
			continue;

		vty_out(vty, " peer-ip add %s%s", ip_str, VTY_NEWLINE);
	}

	vty_out(vty, "!%s", VTY_NEWLINE);
}

DEFUN_NOSH(reload,
		reload_cmd,
		"reload",
		"Restart System\n")
{
	NM_SYSLOG(nm_syslog.system.facility, "system",
			nm_syslog.system.severity, "Reboot - %s", "system");

	if(nm_sync_cluster_configs.enable) {
		nm_sync_cluster_state.rebooting = 1;
		nm_napi_set_cluster_status(&nm_sync_cluster_state);
	}

	usleep(100000);
	system("/sbin/reboot");

	return CMD_SUCCESS;
}

DEFUN_NOSH(nm_shutdown,
		shutdown_cmd,
		"shutdown",
		"Shutdown System\n")
{
	NM_SYSLOG(nm_syslog.system.facility, "system",
			nm_syslog.system.severity, "Shutdown - %s", "system halt");

	if(nm_sync_cluster_configs.enable) {
		nm_sync_cluster_state.rebooting = 1;
		nm_napi_set_cluster_status(&nm_sync_cluster_state);
	}

	usleep(100000);
	system("/sbin/halt");

	return CMD_SUCCESS;
}
