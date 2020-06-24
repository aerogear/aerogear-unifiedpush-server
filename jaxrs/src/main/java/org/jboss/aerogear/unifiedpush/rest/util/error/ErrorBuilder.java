package org.jboss.aerogear.unifiedpush.rest.util.error;

public abstract class ErrorBuilder {
    private ErrorBuilder() {
    }

    public static VariantErrorBuilder forVariants() {
        return new VariantErrorBuilder();
    }

    public static PushApplicationErrorBuilder forPushApplications() {
        return new PushApplicationErrorBuilder();
    }

    public static InstallationErrorBuilder forInstallations() {
        return new InstallationErrorBuilder();
    }

    public static AuthErrorBuilder forAuth() {
        return new AuthErrorBuilder();
    }

    public static HealthCheckErrorBuilder forHealthCheck() {
        return new HealthCheckErrorBuilder();
    }

    public static MetricsErrorBuilder forMetrics() {
        return new MetricsErrorBuilder();
    }


    // Actual builders
    private static class AbstractErrorBuilder<T extends AbstractErrorBuilder<T>> {
        private UnifiedPushError error = new UnifiedPushError("Unknown error");

        protected T setError(UnifiedPushError error) {
            this.error = error;
            return (T)this;
        }

        protected UnifiedPushError getError() {
            return this.error;
        }

        public final UnifiedPushError build() {
            return error;
        }
        public final T withDetail(final String key, final String value) {
            this.error.addDetail(key, value);
            return (T)this;
        }

        public final T withRootException(Throwable exc) {
            this.error.setRootException(exc);
            return (T)this;
        }
    }

    public static class VariantErrorBuilder extends AbstractErrorBuilder<VariantErrorBuilder> {
        private VariantErrorBuilder() {
        }

        public VariantErrorBuilder notFound() {
            return this.setError(new UnifiedPushError("Could not find requested Variant"));
        }

        public VariantErrorBuilder wrongType() {
            return this.setError(new UnifiedPushError("Requested Variant is of another type/platform"));
        }
    }

    public static class PushApplicationErrorBuilder extends AbstractErrorBuilder<PushApplicationErrorBuilder> {
        private PushApplicationErrorBuilder() {
        }

        public PushApplicationErrorBuilder notFound() {
            return this.setError(new UnifiedPushError("Could not find requested PushApplicationEntity"));
        }
    }

    public static class InstallationErrorBuilder extends AbstractErrorBuilder<InstallationErrorBuilder> {
        private InstallationErrorBuilder() {
        }

        public InstallationErrorBuilder notFound() {
            return this.setError(new UnifiedPushError("Could not find requested Installation"));
        }
    }

    public static class AuthErrorBuilder extends AbstractErrorBuilder<AuthErrorBuilder> {
        private AuthErrorBuilder() {
        }

        public AuthErrorBuilder unauthorized() {
            return this.setError(new UnifiedPushError("Unauthorized Request"));
        }
    }

    public static class MetricsErrorBuilder extends AbstractErrorBuilder<MetricsErrorBuilder> {
        private MetricsErrorBuilder() {
        }

        public MetricsErrorBuilder notFound() {
            return this.setError(new UnifiedPushError("Could not find version information"));
        }
    }


    public static class HealthCheckErrorBuilder extends AbstractErrorBuilder<HealthCheckErrorBuilder> {
        private HealthCheckErrorBuilder() {
        }

        public HealthCheckErrorBuilder noVersion() {
            return this.setError(new UnifiedPushError("Could not find version information"));
        }
    }
}
