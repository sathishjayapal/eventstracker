package me.sathish.event_service.domain_event;

import jakarta.validation.Valid;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.security.UserRoles;
import me.sathish.event_service.util.CustomCollectors;
import me.sathish.event_service.util.WebUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/domainEvents")
@PreAuthorize("hasAuthority('" + UserRoles.AUTH_USER + "')")
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
    public String list(final Model model) {
        model.addAttribute("domainEvents", domainEventService.findAll());
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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR, WebUtils.getMessage("domainEvent.create.failure"));
        }
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("domainEvent.create.success"));
        return "redirect:/domainEvents";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("domainEvent", domainEventService.get(id));
        return "domainEvent/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable(name = "id") final Long id,
            @ModelAttribute("domainEvent") @Valid final DomainEventDTO domainEventDTO,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "domainEvent/edit";
        }
        domainEventService.update(id, domainEventDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("domainEvent.update.success"));
        return "redirect:/domainEvents";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id, final RedirectAttributes redirectAttributes) {
        domainEventService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("domainEvent.delete.success"));
        return "redirect:/domainEvents";
    }
}
