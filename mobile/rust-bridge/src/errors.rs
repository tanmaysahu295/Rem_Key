#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum RelayError {
    #[error("invalid desktop address: {details}")]
    InvalidAddress { details: String },
    #[error("invalid certificate fingerprint: {details}")]
    InvalidFingerprint { details: String },
    #[error("invalid pairing code: {details}")]
    InvalidPairingCode { details: String },
    /// The phone's own certificate couldn't be loaded or created.
    #[error("device identity unavailable: {details}")]
    Identity { details: String },
    #[error("already connected or connecting")]
    AlreadyConnected,
    #[error("not connected")]
    NotConnected,
    /// The send queue is full. Only returned for commands that must not be dropped;
    /// pointer and scroll deltas are discarded instead.
    #[error("send queue is full")]
    QueueFull,
}
