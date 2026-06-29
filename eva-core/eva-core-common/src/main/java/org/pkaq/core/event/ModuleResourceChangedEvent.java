package org.pkaq.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 模块资源授权变更事件。
 *
 * @author PKAQ
 */
@Getter
public class ModuleResourceChangedEvent extends ApplicationEvent {

    private final Set<Long> roleIds;

    private final ChangeReason reason;

    public ModuleResourceChangedEvent(Object source, Collection<Long> roleIds, ChangeReason reason) {
        super(source);
        this.roleIds = roleIds == null ? Set.of() : new HashSet<>(roleIds);
        this.reason = reason;
    }

    public ModuleResourceChangedEvent(Object source, Long roleId, ChangeReason reason) {
        this(source, roleId == null ? Set.of() : Set.of(roleId), reason);
    }

    /**
     * 资源授权变更原因。
     */
    public enum ChangeReason {
        MODULE_RESOURCE_CHANGED,
        MODULE_DELETED,
        ROLE_RESOURCE_CHANGED
    }
}
