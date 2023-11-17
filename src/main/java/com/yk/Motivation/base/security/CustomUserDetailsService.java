package com.yk.Motivation.base.security;

import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("username(%s) not found".formatted(username)));

        if (member.isSocialMember()) throw new UsernameNotFoundException("social login user");

        return new User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
    }
}