package me.sathish.event_service.domain_event;

import jakarta.validation.Valid;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.domain.DomainInactiveException;
import me.sathish.event_service.security.UserRoles;
import me.sathish.event_service.util.CustomCollectors;
import me.sathish.event_service.util.WebUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/domainEvents")
@PreAuthorize("hasAnyAuthority('" + UserRoles.AUTH_USER + "', '" + UserRoles.ADMIN + "')")
public class DomainEventController {

    private final DomainEventService domainEventService;
    private final DomainRepository domainRepository;

    public DomainEventController(final DomainEventService domainEventService, final DomainRepository domainRepository) {
        this.domainEventService = domainEventService;
        this.domainRepository = domainRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute(
                "domainValues",
                domainRepository.findAll(Sort.by("id")).stream()
                        .collect(CustomCollectors.toSortedMap(Domain::getId, Domain::getId)));
    }

    @GetMapping
    public String list(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) final Pageable pageable,
            @RequestParam(required = false, defaultValue = "20") final Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") final String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") final String sortDirection,
            final Model model) {
        
        // Create custom pageable with user-selected options
        Pageable customPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageSize,
                Sort.Direction.fromString(sortDirection),
                sortBy
        );
        
        Page<DomainEventDTO> page = domainEventService.findAllPaged(customPageable);
        
        model.addAttribute("domainEvents", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        
        return "domainEvent/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("domainEvent") final DomainEventDTO domainEventDTO) {
        return "domainEvent/add";
    }

    @PostMapping("/add")
    public String add(
            @ModelAttribute("domainEvent") @Valid final DomainEventDTO domainEventDTO,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "domainEvent/add";
        }
        try {
            domainEventService.create(domainEventDTO);
        } catch (DomainInactiveException ex) {
            bindingResult.rejectValue("domain", "domainEvent.domain.inactive", ex.getMessage());
            return "domainEvent/add";
        } catch (Exception e) {
            bindingResult.reject("domainEvent.create.failure", WebUtils.getMessage("domainEvent.create.failure"));
            return "domainEvent/add";
        }
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("domainEvent.create.success"));
        return "redirect:/domainEvents";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("domainEvent", domainEventService.get(id));
        return "domainEvent/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String edit(
            @PathVariable(name = "id") final Long id,
            @ModelAttribute("domainEvent") @Valid final DomainEventDTO domainEventDTO,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "domainEvent/edit";
        }
        try {
            domainEventService.update(id, domainEventDTO);
        } catch (DomainInactiveException ex) {
            bindingResult.rejectValue("domain", "domainEvent.domain.inactive", ex.getMessage());
            return "domainEvent/edit";
        }
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("domainEvent.update.success"));
        return "redirect:/domainEvents";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public String delete(@PathVariable(name = "id") final Long id, final RedirectAttributes redirectAttributes) {
        domainEventService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("domainEvent.delete.success"));
        return "redirect:/domainEvents";
    }
}
