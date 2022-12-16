package client;

import io.micrometer.common.KeyValues;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

import static client.UserIdObservationDocumentation.UserIdHighCardinalityKeyNames.USER_ID;
import static client.UserIdObservationDocumentation.UserIdLowCardinalityKeyNames.USER_TYPE;

class UserIdContext extends Observation.Context{

    private final String userType;
    private final String userId;

    public UserIdContext(String userId,String userType) {
        this.userId = userId;
        this.userType=userType;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }
}

interface UserIdObservationConvention extends ObservationConvention<UserIdContext>
{
    @Override
    default boolean supportsContext(Observation.Context context)
    {
        return context instanceof UserIdContext;
    }
}

class DefaultIdObservationConvention implements UserIdObservationConvention
{
    @Override
    public KeyValues getLowCardinalityKeyValues(UserIdContext context) {
        return KeyValues.of(USER_TYPE.withValue(context.getUserType()));
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(UserIdContext context) {
        return KeyValues.of(USER_ID.withValue(context.getUserId()));
    }

    @Override
    public String getName() {
        return "user.id.name";
    }
}

enum UserIdObservationDocumentation implements ObservationDocumentation
{
    SEND {
        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DefaultIdObservationConvention.class;
        }

        @Override
        public String getContextualName() {
            return "user id";
        }

        @Override
        public String getPrefix() {
            return "userid";
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return UserIdLowCardinalityKeyNames.values();
        }

        @Override
        public KeyName[] getHighCardinalityKeyNames() {
            return UserIdHighCardinalityKeyNames.values();
        }
    };
    enum UserIdHighCardinalityKeyNames implements KeyName {
        USER_ID {
            @Override
            public String asString() {
                return "user.id";
            }
        }
    }

    enum UserIdLowCardinalityKeyNames implements KeyName {
        USER_TYPE {
            @Override
            public String asString() {
                return "user.type";
            }
        }
    }
}