package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AdminDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Admins;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper implements ModelDTOMapper<Admins, AdminDTO>{

    private final PasswordEncoder passwordEncoder;

    public AdminMapper(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminDTO toDTO(Admins admin) {
        if (admin == null) return null;

        AdminDTO dto = new AdminDTO();
        dto.setName(admin.getName());
        dto.setEmail(admin.getEmail());
        return dto;
    }

    @Override
    public Admins toModel(AdminDTO dto) {
        if (dto == null) return null;

        Admins admin = new Admins();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());

        Account account = new Account();
        account.setUsername(dto.getEmail());
        account.setPassword(passwordEncoder.encode(dto.getPassword())); //encode password;
        account.setType(Account.AccountType.ADMIN);
        admin.setAccount(account);
        return admin;
    }
}
