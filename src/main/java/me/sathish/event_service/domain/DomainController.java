package me.sathish.event_service.domain;

import jakarta.validation.Valid;
import me.sathish.event_service.security.UserRoles;
import me.sathish.event_service.util.ReferencedWarning;
import me.sathish.event_service.util.WebUtils;
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
@RequestMapping("/domains")
@PreAuthorize("hasAnyAuthority('" + UserRoles.AUTH_USER + "', '" + UserRoles.ADMIN + "')")
public class DomainController {

    private final DomainService domainService;

    public DomainController(final DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("domains", domainService.findAll());
        return "domain/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("domain") final DomainDTO domainDTO) {
        return "domain/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("domain") @Valid final DomainDTO domainDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "domain/add";
        }
        domainService.create(domainDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("domain.create.success"));
        return "redirect:/domains";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("domain", domainService.get(id));
        return "domain/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id,
            @ModelAttribute("domain") @Valid final DomainDTO domainDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "domain/edit";
        }
        domainService.update(id, domainDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("domain.update.success"));
        return "redirect:/domains";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = domainService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            domainService.delete(id);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("domain.delete.success"));
        }
        return "redirect:/domains";
    }

}
