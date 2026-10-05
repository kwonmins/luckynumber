/*
 * Drop-in replacements for nserver cluster handlers after changing:
 *
 *   struct nm_sync_cluster_configs      -> struct nm_sync_cluster_config
 *   struct nm_sync_cluster_peer_configs -> removed; peer list is in config.peer
 *   runtime fields                     -> struct nm_sync_cluster_status
 *
 * Assumed appDemo/socket-side globals:
 *   struct nm_sync_cluster_config nm_sync_cluster_config;
 *   struct nm_sync_cluster_status nm_sync_cluster_status;
 *
 * Also update prototypes:
 *   int nm_socket_cluster_config_set(struct nm_sync_cluster_config *config);
 *   int nm_sync_cluster_make_tx_data(struct nm_sync_cluster_peer_status *data);
 * or keep your TX packet type separate if you split wire packet/status types.
 */

static void nm_nserv_cluster_send_once(void)
{
	int sock;

	sock = nm_socket_cluster_send_open();
	if(sock < 0)
		return;

	nm_socket_cluster_send_packet(sock);
	close(sock);
}

int nm_nserv_set_cluster_config(int fd, int cmd, void *p, int plen)
{
	int rc = 0;
	struct nm_sync_cluster_config config;

	if(!p) {
		rc = -1;
		goto _exit;
	}

	if(plen != sizeof(config)) {
		rc = -1;
		goto _exit;
	}

	memcpy(&config, p, sizeof(config));

	/*
	 * nm_socket_cluster_config_set() should copy config into the appDemo
	 * global config, recover local state when disabled, and optionally send
	 * one immediate state packet.
	 */
	rc = nm_socket_cluster_config_set(&config);

_exit:
	return nm_nserv_send_return(fd, cmd, rc);
}

int nm_nserv_get_cluster_config(int fd, int cmd, void *p, int plen)
{
	int rc = 0;
	struct nm_sync_cluster_config config;

	memset(&config, 0, sizeof(config));
	memcpy(&config, &nm_sync_cluster_config, sizeof(config));

	return nm_nserv_send_data_return(fd, cmd, rc,
			&config, sizeof(config));
}

int nm_nserv_set_cluster_status(int fd, int cmd, void *p, int plen)
{
	int rc = 0;
	struct nm_sync_cluster_status status;

	if(!p) {
		rc = -1;
		goto _exit;
	}

	if(plen != sizeof(status)) {
		rc = -1;
		goto _exit;
	}

	memcpy(&status, p, sizeof(status));

	/*
	 * Zebra does not own peer runtime status. Do not overwrite
	 * fail_action_active or peer_status here. This command is only used
	 * as a reboot/shutdown event from Zebra to appDemo.
	 */
	nm_sync_cluster_status.rebooting = status.rebooting ? 1 : 0;

	if(nm_sync_cluster_status.rebooting && nm_sync_cluster_config.enable)
		nm_nserv_cluster_send_once();

_exit:
	return nm_nserv_send_return(fd, cmd, rc);
}

int nm_nserv_get_cluster_status(int fd, int cmd, void *p, int plen)
{
	int rc = 0;
	struct nm_sync_cluster_status status;

	memset(&status, 0, sizeof(status));
	memcpy(&status, &nm_sync_cluster_status, sizeof(status));

	return nm_nserv_send_data_return(fd, cmd, rc,
			&status, sizeof(status));
}

/*
 * Replace the cluster cases in nm_socket_process() with:
 *
 *   case NM_NSERV_SET_CLUSTER_CONFIG:
 *       return nm_nserv_set_cluster_config(fd, cmd, p, plen);
 *   case NM_NSERV_GET_CLUSTER_CONFIG:
 *       return nm_nserv_get_cluster_config(fd, cmd, p, plen);
 *   case NM_NSERV_SET_CLUSTER_STATUS:
 *       return nm_nserv_set_cluster_status(fd, cmd, p, plen);
 *   case NM_NSERV_GET_CLUSTER_STATUS:
 *       return nm_nserv_get_cluster_status(fd, cmd, p, plen);
 *
 * Remove old peer-config nserver commands if they still exist:
 *
 *   NM_NSERV_SET_CLUSTER_PEER_CONFIG
 *   NM_NSERV_GET_CLUSTER_PEER_CONFIG
 */
