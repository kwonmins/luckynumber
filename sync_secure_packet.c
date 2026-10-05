/*
 * Secure sync packet helper.
 *
 * Packet format:
 *
 *   [ sync_header ][ nonce ][ ciphertext ][ auth_tag ]
 *
 * - sync_header is sent in plain text, but it is authenticated as AAD.
 * - sync_payload is serialized and encrypted.
 * - AES-256-GCM provides encryption and integrity protection, so a separate
 *   HMAC field is not needed on the wire.
 *
 * Build example:
 *
 *   gcc -Wall -Wextra -c sync_secure_packet.c
 *   gcc -o sync_test sync_secure_packet.c -lcrypto
 */

#include <arpa/inet.h>
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <stdint.h>
#include <stddef.h>
#include <string.h>

#define SYNC_VERSION                 1
#define SYNC_MAGIC                   0xa55a

#define SYNC_HEADER_LEN              28
#define SYNC_NONCE_LEN               12
#define SYNC_TAG_LEN                 16
#define SYNC_KEY_LEN                 32

#define SYNC_PAYLOAD_PLAIN_LEN       10
#define SYNC_PACKET_MAX_LEN          1500

#define SYNC_TYPE_STATUS             1

struct sync_header {
	uint16_t version;
	uint16_t magic;
	uint16_t type;
	uint16_t payload_len;
	uint64_t boot_id;
	uint64_t seq_number;
	uint64_t ack_number;
};

struct sync_payload {
	uint16_t tool1_status;
	uint16_t tool2_status;
	uint16_t tool1_operate_status;
	uint16_t tool2_operate_status;
	uint16_t connect;
};

enum sync_packet_error {
	SYNC_PACKET_OK = 0,
	SYNC_PACKET_ERR_ARG = -1,
	SYNC_PACKET_ERR_SHORT = -2,
	SYNC_PACKET_ERR_MAGIC = -3,
	SYNC_PACKET_ERR_LEN = -4,
	SYNC_PACKET_ERR_RANDOM = -5,
	SYNC_PACKET_ERR_CRYPTO = -6,
	SYNC_PACKET_ERR_AUTH = -7
};

static uint64_t sync_bswap64(uint64_t v)
{
	return ((v & 0x00000000000000ffULL) << 56) |
		((v & 0x000000000000ff00ULL) << 40) |
		((v & 0x0000000000ff0000ULL) << 24) |
		((v & 0x00000000ff000000ULL) << 8) |
		((v & 0x000000ff00000000ULL) >> 8) |
		((v & 0x0000ff0000000000ULL) >> 24) |
		((v & 0x00ff000000000000ULL) >> 40) |
		((v & 0xff00000000000000ULL) >> 56);
}

static uint64_t sync_htonll(uint64_t v)
{
	static const uint16_t one = 1;

	if(*(const uint8_t *)&one == 1)
		return sync_bswap64(v);

	return v;
}

static uint64_t sync_ntohll(uint64_t v)
{
	return sync_htonll(v);
}

static void sync_put_u16(uint8_t **p, uint16_t v)
{
	uint16_t n;

	n = htons(v);
	memcpy(*p, &n, sizeof(n));
	*p += sizeof(n);
}

static void sync_put_u64(uint8_t **p, uint64_t v)
{
	uint64_t n;

	n = sync_htonll(v);
	memcpy(*p, &n, sizeof(n));
	*p += sizeof(n);
}

static uint16_t sync_get_u16(const uint8_t **p)
{
	uint16_t n;

	memcpy(&n, *p, sizeof(n));
	*p += sizeof(n);

	return ntohs(n);
}

static uint64_t sync_get_u64(const uint8_t **p)
{
	uint64_t n;

	memcpy(&n, *p, sizeof(n));
	*p += sizeof(n);

	return sync_ntohll(n);
}

int sync_header_encode(uint8_t *out, size_t out_len,
		const struct sync_header *header)
{
	uint8_t *p;

	if(!out || !header)
		return SYNC_PACKET_ERR_ARG;

	if(out_len < SYNC_HEADER_LEN)
		return SYNC_PACKET_ERR_SHORT;

	p = out;

	sync_put_u16(&p, header->version);
	sync_put_u16(&p, header->magic);
	sync_put_u16(&p, header->type);
	sync_put_u16(&p, header->payload_len);
	sync_put_u64(&p, header->boot_id);
	sync_put_u64(&p, header->seq_number);
	sync_put_u64(&p, header->ack_number);

	return (int)(p - out);
}

int sync_header_decode(struct sync_header *header,
		const uint8_t *in, size_t in_len)
{
	const uint8_t *p;

	if(!header || !in)
		return SYNC_PACKET_ERR_ARG;

	if(in_len < SYNC_HEADER_LEN)
		return SYNC_PACKET_ERR_SHORT;

	p = in;

	header->version = sync_get_u16(&p);
	header->magic = sync_get_u16(&p);
	header->type = sync_get_u16(&p);
	header->payload_len = sync_get_u16(&p);
	header->boot_id = sync_get_u64(&p);
	header->seq_number = sync_get_u64(&p);
	header->ack_number = sync_get_u64(&p);

	if(header->version != SYNC_VERSION || header->magic != SYNC_MAGIC)
		return SYNC_PACKET_ERR_MAGIC;

	return (int)(p - in);
}

int sync_payload_encode(uint8_t *out, size_t out_len,
		const struct sync_payload *payload)
{
	uint8_t *p;

	if(!out || !payload)
		return SYNC_PACKET_ERR_ARG;

	if(out_len < SYNC_PAYLOAD_PLAIN_LEN)
		return SYNC_PACKET_ERR_SHORT;

	p = out;

	sync_put_u16(&p, payload->tool1_status);
	sync_put_u16(&p, payload->tool2_status);
	sync_put_u16(&p, payload->tool1_operate_status);
	sync_put_u16(&p, payload->tool2_operate_status);
	sync_put_u16(&p, payload->connect);

	return (int)(p - out);
}

int sync_payload_decode(struct sync_payload *payload,
		const uint8_t *in, size_t in_len)
{
	const uint8_t *p;

	if(!payload || !in)
		return SYNC_PACKET_ERR_ARG;

	if(in_len != SYNC_PAYLOAD_PLAIN_LEN)
		return SYNC_PACKET_ERR_LEN;

	p = in;

	payload->tool1_status = sync_get_u16(&p);
	payload->tool2_status = sync_get_u16(&p);
	payload->tool1_operate_status = sync_get_u16(&p);
	payload->tool2_operate_status = sync_get_u16(&p);
	payload->connect = sync_get_u16(&p);

	return (int)(p - in);
}

int sync_payload_encrypt(uint8_t *ciphertext, size_t ciphertext_len,
		size_t *written_len, uint8_t tag[SYNC_TAG_LEN],
		const uint8_t key[SYNC_KEY_LEN],
		const uint8_t nonce[SYNC_NONCE_LEN],
		const uint8_t *aad, size_t aad_len,
		const uint8_t *plaintext, size_t plaintext_len)
{
	int len;
	int total = 0;
	EVP_CIPHER_CTX *ctx;

	if(!ciphertext || !written_len || !tag || !key || !nonce ||
			!plaintext)
		return SYNC_PACKET_ERR_ARG;

	if(ciphertext_len < plaintext_len)
		return SYNC_PACKET_ERR_SHORT;

	ctx = EVP_CIPHER_CTX_new();
	if(!ctx)
		return SYNC_PACKET_ERR_CRYPTO;

	if(EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, NULL, NULL) != 1)
		goto crypto_error;

	if(EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN,
				SYNC_NONCE_LEN, NULL) != 1)
		goto crypto_error;

	if(EVP_EncryptInit_ex(ctx, NULL, NULL, key, nonce) != 1)
		goto crypto_error;

	if(aad && aad_len > 0) {
		if(EVP_EncryptUpdate(ctx, NULL, &len, aad,
					(int)aad_len) != 1)
			goto crypto_error;
	}

	if(EVP_EncryptUpdate(ctx, ciphertext, &len, plaintext,
				(int)plaintext_len) != 1)
		goto crypto_error;

	total = len;

	if(EVP_EncryptFinal_ex(ctx, ciphertext + total, &len) != 1)
		goto crypto_error;

	total += len;

	if(EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG,
				SYNC_TAG_LEN, tag) != 1)
		goto crypto_error;

	EVP_CIPHER_CTX_free(ctx);
	*written_len = (size_t)total;

	return SYNC_PACKET_OK;

crypto_error:
	EVP_CIPHER_CTX_free(ctx);
	return SYNC_PACKET_ERR_CRYPTO;
}

int sync_payload_decrypt(uint8_t *plaintext, size_t plaintext_len,
		size_t *written_len, const uint8_t tag[SYNC_TAG_LEN],
		const uint8_t key[SYNC_KEY_LEN],
		const uint8_t nonce[SYNC_NONCE_LEN],
		const uint8_t *aad, size_t aad_len,
		const uint8_t *ciphertext, size_t ciphertext_len)
{
	int len;
	int total = 0;
	int rc;
	EVP_CIPHER_CTX *ctx;

	if(!plaintext || !written_len || !tag || !key || !nonce ||
			!ciphertext)
		return SYNC_PACKET_ERR_ARG;

	if(plaintext_len < ciphertext_len)
		return SYNC_PACKET_ERR_SHORT;

	ctx = EVP_CIPHER_CTX_new();
	if(!ctx)
		return SYNC_PACKET_ERR_CRYPTO;

	if(EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, NULL, NULL) != 1)
		goto crypto_error;

	if(EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN,
				SYNC_NONCE_LEN, NULL) != 1)
		goto crypto_error;

	if(EVP_DecryptInit_ex(ctx, NULL, NULL, key, nonce) != 1)
		goto crypto_error;

	if(aad && aad_len > 0) {
		if(EVP_DecryptUpdate(ctx, NULL, &len, aad,
					(int)aad_len) != 1)
			goto crypto_error;
	}

	if(EVP_DecryptUpdate(ctx, plaintext, &len, ciphertext,
				(int)ciphertext_len) != 1)
		goto crypto_error;

	total = len;

	if(EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_TAG,
				SYNC_TAG_LEN, (void *)tag) != 1)
		goto crypto_error;

	rc = EVP_DecryptFinal_ex(ctx, plaintext + total, &len);
	if(rc != 1) {
		EVP_CIPHER_CTX_free(ctx);
		return SYNC_PACKET_ERR_AUTH;
	}

	total += len;

	EVP_CIPHER_CTX_free(ctx);
	*written_len = (size_t)total;

	return SYNC_PACKET_OK;

crypto_error:
	EVP_CIPHER_CTX_free(ctx);
	return SYNC_PACKET_ERR_CRYPTO;
}

int sync_packet_make(uint8_t *packet, size_t packet_len,
		size_t *written_len, struct sync_header *header,
		const struct sync_payload *payload,
		const uint8_t key[SYNC_KEY_LEN])
{
	int rc;
	int header_len;
	int plain_len;
	size_t cipher_len = 0;
	uint8_t plain[SYNC_PAYLOAD_PLAIN_LEN];
	uint8_t *nonce;
	uint8_t *ciphertext;
	uint8_t *tag;

	if(!packet || !written_len || !header || !payload || !key)
		return SYNC_PACKET_ERR_ARG;

	if(packet_len < SYNC_HEADER_LEN + SYNC_NONCE_LEN +
			SYNC_PAYLOAD_PLAIN_LEN + SYNC_TAG_LEN)
		return SYNC_PACKET_ERR_SHORT;

	memset(plain, 0, sizeof(plain));

	plain_len = sync_payload_encode(plain, sizeof(plain), payload);
	if(plain_len < 0)
		return plain_len;

	header->version = SYNC_VERSION;
	header->magic = SYNC_MAGIC;
	header->payload_len = SYNC_NONCE_LEN + plain_len + SYNC_TAG_LEN;

	header_len = sync_header_encode(packet, packet_len, header);
	if(header_len < 0)
		return header_len;

	nonce = packet + header_len;
	ciphertext = nonce + SYNC_NONCE_LEN;
	tag = ciphertext + plain_len;

	if(RAND_bytes(nonce, SYNC_NONCE_LEN) != 1)
		return SYNC_PACKET_ERR_RANDOM;

	rc = sync_payload_encrypt(ciphertext,
			packet_len - header_len - SYNC_NONCE_LEN - SYNC_TAG_LEN,
			&cipher_len, tag, key, nonce, packet, header_len,
			plain, (size_t)plain_len);
	if(rc < 0)
		return rc;

	*written_len = (size_t)header_len + SYNC_NONCE_LEN +
		cipher_len + SYNC_TAG_LEN;

	return SYNC_PACKET_OK;
}

int sync_packet_parse(struct sync_header *header,
		struct sync_payload *payload, const uint8_t *packet,
		size_t packet_len, const uint8_t key[SYNC_KEY_LEN])
{
	int rc;
	int header_len;
	size_t cipher_len;
	size_t plain_len = 0;
	const uint8_t *nonce;
	const uint8_t *ciphertext;
	const uint8_t *tag;
	uint8_t plain[SYNC_PAYLOAD_PLAIN_LEN];

	if(!header || !payload || !packet || !key)
		return SYNC_PACKET_ERR_ARG;

	if(packet_len < SYNC_HEADER_LEN + SYNC_NONCE_LEN + SYNC_TAG_LEN)
		return SYNC_PACKET_ERR_SHORT;

	header_len = sync_header_decode(header, packet, packet_len);
	if(header_len < 0)
		return header_len;

	if((size_t)header_len + header->payload_len != packet_len)
		return SYNC_PACKET_ERR_LEN;

	if(header->payload_len < SYNC_NONCE_LEN + SYNC_TAG_LEN)
		return SYNC_PACKET_ERR_LEN;

	cipher_len = header->payload_len - SYNC_NONCE_LEN - SYNC_TAG_LEN;
	if(cipher_len != SYNC_PAYLOAD_PLAIN_LEN)
		return SYNC_PACKET_ERR_LEN;

	nonce = packet + header_len;
	ciphertext = nonce + SYNC_NONCE_LEN;
	tag = ciphertext + cipher_len;

	memset(plain, 0, sizeof(plain));

	rc = sync_payload_decrypt(plain, sizeof(plain), &plain_len,
			tag, key, nonce, packet, (size_t)header_len,
			ciphertext, cipher_len);
	if(rc < 0)
		return rc;

	if(plain_len != SYNC_PAYLOAD_PLAIN_LEN)
		return SYNC_PACKET_ERR_LEN;

	return sync_payload_decode(payload, plain, plain_len);
}

int sync_packet_header_len(void)
{
	return SYNC_HEADER_LEN;
}

int sync_packet_max_len(void)
{
	return SYNC_PACKET_MAX_LEN;
}

